# -*- coding: utf-8 -*-
from math import inf
import sys
import os
import json
import time

from ctypes import *
from multiprocessing import Pool

import xml_parser_order as xpOrder
import c_lib_warpper as cw
import logger_json as log

# def multiProcessOrbitPropAndCloseApproach( i, N_LEAP, pyLEAPS, N_EOP, pyEOPS, N_SWD, py_swdata, obsTimeObj, satInfo_s, lth_step ):
#     print('i=', i)
#     LEAPS = cw.pyLEAPS2C(N_LEAP, pyLEAPS)

#     EOPS = cw.pyDEOP2C(N_EOP, pyEOPS)

#     swdata = cw.pyDSWData2c(N_SWD, py_swdata)

#     objSateph_s = cw.orbitProp( N_LEAP, LEAPS, N_EOP, EOPS, swdata,
#                             obsTimeObj, satInfo_s,
#                             lth_step )
#     # print(objSateph_s)
#     # objSateph_s_list.append(objSateph_s)
#     return objSateph_s

if __name__ == "__main__":
    # 运行启动时间
    # t0 = time.time()

    # 读取参数
    if len(sys.argv) > 1:
        # 读取订单文件路径
        filepath_order = sys.argv[1]

        # 如果订单文件存在 ...
        if not (os.path.exists(filepath_order) ):
            # print("Error: 订单文件不存在")
            log.error('Order file does NOT found.', filepath_order)
            exit()
        else:
            #------------------------------------------------------------
            # 解析订单
            log.progress('Loading order file ...', filepath_order)

            orderinfo  = xpOrder.parse( filepath_order )

            # 订单信息
            task_type = orderinfo['task']['type']
            task_order_id = orderinfo['task']['order_id']
            log.progress('Order file loaded. Initializing ...', task_order_id)

            # 外推时间参数
            time_start_utc = orderinfo['params']['time_start_utc']
            time_unit = orderinfo['params']['time_unit']
            time_span = orderinfo['params']['time_span']

            # 主目标（卫星）文件路径
            path_sat = orderinfo['input']['path_satellite']

            # 从目标文件路径list
            path_target_list = orderinfo['input']['path_target_orbit_list']
            # print(path_target_list)

            # 从目标雷达数据文件路径list
            path_target_radar_list = orderinfo['input']['path_target_radar_list']

            # 参数文件路径
            path_leap = orderinfo['params']['path_leap']
            path_eop = orderinfo['params']['path_eop']
            path_swd = orderinfo['params']['path_swd']
            path_error = orderinfo['params']['path_error']
            path_gates = orderinfo['params']['path_gates']

            #------------------------------------------------------------
            # 从内部文件获得内部参数
            path_internal_settings = './settings/c_lib_param.json'
            if not os.path.exists(path_internal_settings):
                log.error('Internal parameters setting file does NOT found.', path_internal_settings)
                exit()
                # print("ERROR: 缺少内部参数! 路径: '%s' 不存在!\n"%(path_internal_settings) )

            fh = open(path_internal_settings, 'r', encoding='utf-8')
            lines = fh.readlines()
            fh.close()

            # 拼接json子串
            jsonData = ''
            for oneline in lines:
                jsonData += oneline

            # 转换为对象
            internalParamsObj = json.loads(jsonData)

            lth_step = internalParamsObj['lth_step']        # 星历外推步长, 即相邻两组星历的历元时间间隔[sec]
            coeff = internalParamsObj['coeff']              # 
            gate_deltT = internalParamsObj['gate_deltT'] 
            gate_deltV = internalParamsObj['gate_deltV'] 
            lambda_ = internalParamsObj['lambda_itra'] 
            eps = internalParamsObj['eps_itra'] 
            NREV_S = internalParamsObj['NREV_S'] 
            NREV_T = internalParamsObj['NREV_T'] 
            scale = internalParamsObj['scale'] 
            
            # ------------------------------------------------------------
            # 从门限文件获得门限参数
            # 判断门限文件是否存在
            if not os.path.exists(path_gates):
                # print("ERROR: 门限文件无法读取! 路径: '%s' 不存在!\n"%(path_gates) )
                log.error('Threadhold setting file does NOT found.', path_gates)
                exit()

            # 读取门限参数
            fh = open(path_gates, 'r', encoding='utf-8')
            lines = fh.readlines()
            fh.close()

            # 拼接json字串
            jsonData = ''
            for oneline in lines:
                jsonData += oneline

            # 转换为对象
            gatesParamsObj = json.loads(jsonData)

            # 获得门限参数
            gate_dr = gatesParamsObj['gate_dr']
            gate_dSTW = gatesParamsObj['gate_dSTW']
            gate_etca = gatesParamsObj['gate_etca']
            gate_pc = gatesParamsObj['gate_pc']

            # print(gate_dSTW)
            # print(gate_dr)
            # print(gate_etca)
            # print(gate_pc)

            #------------------------------------------------------------
            # 解析观测时间范围
            obsTimeObj = cw.getStartTime( time_start_utc, time_span )
            # print(obsTimeObj)

            # 读取持续时间dt
            dt = obsTimeObj['dt']
            # 读取jutc时间范围
            jutcStart = obsTimeObj['jutcs']
            jutcEnd = obsTimeObj['jutce']

            #------------------------------------------------------------
            # 解析公共参数
            
            N_LEAP, LEAPS = cw.loadLeap( path_leap )
            pyLEAPS = cw.LEAPS2Py( LEAPS )

            N_EOP, EOPS = cw.loadEOP( path_eop, jutcStart, jutcEnd )
            pyEOPS = cw.DEOP2Py( EOPS )

            swdata = cw.loadSpaceWeather( path_swd, jutcStart, jutcEnd )
            N_SWD = len( swdata )
            py_swdata = cw.DSWData2Py(swdata)

            orbit_prop_error_table = cw.loadOrbitPropError(path_error)
            # print(orbit_prop_error_table)
            log.progress('Public parameters file loaded.', '')

            #------------------------------------------------------------
            # 创建时间表数组

            NEPK, Array_TT = cw.getArrayTT ( N_LEAP, LEAPS, N_EOP, EOPS, obsTimeObj, lth_step )
            start_jtt = Array_TT[0].JTT

            # print("start_jtt=", start_jtt)

            #------------------------------------------------------------
            # 读取目标文件
            
            # 读取主目标文件, 得到自定义三行数据
            flag, sat3lines_p = cw.loadSatOrbitFile(path_sat)
            sat3lines_p = sat3lines_p[0]
            log.progress('Primary target info loaded.', path_sat)

            # 读取从目标文件, 得到自定义三行数据, 并合并到统一列表 sat3lines_s_list
            
            sat3lines_s_list = []
            for oneTargetfile in path_target_list:
                # print("oneTargetfile", oneTargetfile)
                flag, sat3lines_s = cw.loadSatOrbitFile(oneTargetfile)
                # print( flag, sat3lines_s )
                if flag == 0:   # 文件存在时
                    sat3lines_s_list += sat3lines_s
            log.progress('Secondary target(s) info loaded.', path_target_list)
            # print("sat3lines_s_list")
            # print(sat3lines_s_list)
            # print(len(sat3lines_s_list))

            # 解析自定义三行数据, 得到tle或kepler轨道数据
            # 解析主目标
            satInfo_p = cw.decode3LinesOrbitParam( sat3lines_p )
            # print( satInfo_p )

            # 解析从目标
            satInfo_s_list = []
            for sat3lines_s in sat3lines_s_list:
                satInfo_s = cw.decode3LinesOrbitParam( sat3lines_s )
                satInfo_s_list.append(satInfo_s)

            # print( satInfo_s_list )

            #------------------------------------------------------------
            # 目标轨道外推
            # 主目标轨道数据外推
            objSateph_p = cw.orbitProp( N_LEAP, LEAPS, N_EOP, EOPS, swdata,
                                        NEPK, Array_TT,
                                        satInfo_p )

            satephs_p = objSateph_p['sateph']
            satid_p = objSateph_p['satid']
            log.progress('Primary target: Orbital propagation finished.', satid_p)
            # print( satInfo_p )
            #------------------------------------------------------------
            # 从目标轨道数据外推 并行
            # ts = time.time()

            # pool = Pool(processes=4)
            # results = []

            # n = len( satInfo_s_list )
            # print('n=', n)

            # for i in range(n):
                # res = pool.apply_async( multiProcessOrbitPropAndCloseApproach, 
                #                         (i, N_LEAP, pyLEAPS, N_EOP, pyEOPS, N_SWD, py_swdata, obsTimeObj, satInfo_s_list[i], lth_step,)
                #                       )
            #     results.append(res)
            # pool.close()    # 关闭进程池，不能再添加进程
            # pool.join()     # 主进程等待所有进程执行完毕

            # te = time.time()
            # print("并行 time cost = ", te-ts)

            # objSateph_s_list = []

            # for res in results:
            #     objSateph_s = res.get()

            #     n = objSateph_s['n_sateph']
            #     str = objSateph_s['sateph_str']
            #     sateph = cw.pySateph2C( n, str )

            #     # for i in range(n):
            #     #     print(i, sateph[i].jtt)

            #     objSateph_s['sateph'] = sateph
            #     objSateph_s['sateph_str'] = ''
            #     objSateph_s_list.append( objSateph_s )

            # print( objSateph_s_list )

            #------------------------------------------------------------
            # 从目标数据处理
            # ts = time.time()
            # i = 0
            satInfo_sca_list = []
            objSateph_s_list = []
            for satInfo_s in satInfo_s_list:
                # print('从目标', i)
                # i+=1
                # 从目标轨道数据外推
                objSateph_s = cw.orbitProp( N_LEAP, LEAPS, N_EOP, EOPS, swdata,
                                        NEPK, Array_TT, 
                                        satInfo_s )
                satid_s = objSateph_s['satid']
                log.progress('Secondary target: Orbital propagation finished.', satid_s)
                
                # 存入从目标列表
                objSateph_s_list.append(objSateph_s)

                # 主-从目标交会接近筛查, 得到接近事件
                n_ca_rst, ca_rst = cw.Filter_CloseApproach( N_LEAP, LEAPS, N_EOP, EOPS, NEPK, Array_TT,
                                         objSateph_p, objSateph_s,
                                         gate_dr, lth_step, dt, orbit_prop_error_table )
                eventObj = {
                    'satid':satid_s,
                    'count':n_ca_rst
                }
                log.progress('Secondary target: Close approach events.', eventObj)
                # 构造告警等级产品
                # 对每个接近事件进行告警等级评估
                alarmObj_list = []
                max_alarm_level = 0
                choosen_alarmObj = {
                    'satid_s': satid_s,
                    'max': 0,
                    'jtt_ca': 0,
                    'dswt': 0, 
                    'dr': 0, 
                    'pc': 0
                }
                for j in range(n_ca_rst):
                    alarmObj = cw.getAlarmLevel_OrbitCA( 
                                    ca_rst[j],
                                    start_jtt,
                                    gate_dr, gate_dSTW, gate_etca, gate_pc
                                )
                    # print(alarmObj)
                    alarmObj_list.append( alarmObj )
                    # 记录最大告警等级
                    this_alarm_level = alarmObj['max']
                    if this_alarm_level > max_alarm_level:
                        max_alarm_level = this_alarm_level
                        choosen_alarmObj = alarmObj
            
                log.result('Secondary target: Orbital Alarm.', choosen_alarmObj)
                    # print(j, alarmObj)
                
                # 构造 轨道数据+接近事件+威胁等级 统一对象
                satInfo_sca = {
                    'objSateph':objSateph_s,
                    'objCa':{
                        'n_ca_rst':n_ca_rst,
                        'ca_rst':ca_rst
                    },
                    'objAlarm':alarmObj_list,
                }
                satInfo_sca_list.append(satInfo_sca)

            # te = time.time()
            # print("串行 time cost = ", te-ts)
            # print(satInfo_sca_list)
            # for satInfo_sca in satInfo_sca_list:
            #     print(satInfo_sca)

            #------------------------------------------------------------
            # 处理雷达数据

            # for path_target_radar in path_target_radar_list:
            #     print( path_target_radar )
            satInfo_radar_sca_list = []

            #------------------------------------------------------------
            # 合并 轨道数据+接近事件+威胁等级 统一对象
            satInfo_sca_all = []
            satInfo_sca_all += satInfo_sca_list
            satInfo_sca_all += satInfo_radar_sca_list
            
            # 记录需要进行规避策略的从目标
            # 从目标在目标list中的index
            keySatIndex = -1
            # 接近事件在该从目标接近事件list中的index
            keyCaIndex = -1

            # 记录最近碰撞时间
            min_jtt_ca = inf

            # 遍历全部目标
            for i in range(len(satInfo_sca_all)):
                # 判断最高危险等级
                # 获得从目标风险信息对象
                objAlarm = satInfo_sca_all[i]['objAlarm']
                # 遍历该目标全部接近事件
                for j in range(len(objAlarm)):
                    # print( i, j, objAlarm[j]['jtt_ca'], "vs", min_jtt_ca )
                    # 如果最大风险等级达到3, 需要计算规避策略
                    if objAlarm[j]['max'] == 3:
                        # 获得当前接近事件的发生时间
                        jtt_ca = objAlarm[j]['jtt_ca']
                        # 找到最早发生的接近事件
                        if jtt_ca < min_jtt_ca:
                            # 更新最早发生的接近事件时间
                            min_jtt_ca = jtt_ca
                            # 记录该事件
                            keySatIndex = i
                            keyCaIndex = j

            # 找到需要处理的ca事件
            key_ca_rst = satInfo_sca_all[keySatIndex]['objCa']['ca_rst'][keyCaIndex]

            # print(  
            #     key_ca_rst.satid_p, key_ca_rst.satid_s,
            #     key_ca_rst.jtt_ca, 
            #     key_ca_rst.dr_stw[0], key_ca_rst.dr_stw[1], key_ca_rst.dr_stw[2]
            #     )

            # 
            # print("keySatIndex=", keySatIndex)
            # print("keyCaIndex=", keyCaIndex)

            #------------------------------------------------------------
            # 计算规避策略
            path_mv = './temp/temp_avoidance_strategy.txt'

            JTT_ref = Array_TT[0].JTT

            asObj = cw.getAvoidanceStrategy( 
                N_LEAP, LEAPS, N_EOP, EOPS, swdata, NEPK, Array_TT,
                coeff, gate_dr, gate_dSTW,
                gate_deltT, gate_deltV, lambda_, eps, NREV_S, NREV_T, scale,
                lth_step,
                path_mv, 
                key_ca_rst, JTT_ref,
                objSateph_p, objSateph_s_list
            )

            # print(mvList)
            log.result('Avoidance strategy', asObj)

    else:
        log.error('NOT enough arguments.', '')
        exit()