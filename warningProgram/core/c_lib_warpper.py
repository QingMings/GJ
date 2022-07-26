# -*- coding: utf-8 -*-
from ctypes import *
from math import sqrt, cos, sin, ceil
from random import random
import os
import platform
import logger_json as log

# 判断操作系统
c_lib_path = ''
if (platform.system() == 'Windows'):
    c_lib_path = "./lib/Dll_GEOAlarm.dll"
else:
    c_lib_path = "./lib/libDll_GEOAlarm.so"

# 判断C lib是否存在
if not (os.path.exists(c_lib_path) ):
    # print("Error: 订单文件不存在")
    log.error('C_lib does NOT found.', c_lib_path)
    exit()

# global
STRLTH_MAX      = 600   # 自定义字符串最长长度(公用)
NMAX_LEAP       = 50	# LEAP结构体组数最大值,记录从19720101开始的跳秒组数.
HalfLen_EOP     = 100	# EOP结构体组数最大值,参考时间(日期)前后一段时间的EOP值.
HalfLen_Flux    = 100	# SWData结构体组数最大值,参考时间(日期)前后一段时间的SWData值.
LTH_TLE         = 70	# TLE根数每行字符数(包含字符串结束符'\0')
NMAX_CA_RST     = 280	# 7天内每类接近极值的数量
NPOL_MAX        = 7	    # 多项式内插阶数

PI              = 3.1415926535897932385
Rad2Deg         = 180.0 / PI
Deg2Rad         = PI / 180.0
MU              = 3.986004415E+14   # 地球引力常数[m^3/s^2]; JGM-3

# 创建 DLEAP 结构体原型
class DLEAP(Structure):
    _fields_ = [
        ("YrMoDy",  c_int),
        ("TAI_UTC", c_double)       # [sec]
    ]

# 创建 DEOP 结构体原型
class DEOP(Structure):
    _fields_ = [
        ("MJD",     c_double),      # 约简儒略日(UTC时间)
        ("UT1_UTC", c_double),      # [sec]
        ("xp",      c_double),      # [rad]
        ("yp",      c_double)       # [rad]
]

# 创建 DSWData 结构体原型
class DSWData(Structure):
    _fields_ = [
        ("mjd",         c_double),      # 约简儒略日, FAPKP[0]
        ("Bf_Last3",    c_double),      # 当天前3个月辐射流量平均值, FAPKP[14]
        ("Bf_Center6",  c_double),      # 当天前后6个月辐射流量平均值, FAPKP[1]
        ("Bf_Center3",  c_double),      # 当天前后3个月辐射流量平均值, FAPKP[2]
        ("fo",          c_double),      # 10.7cm太阳辐射流量观测值, FAPKP[12]
        ("fau",         c_double),      # 10.7cm太阳辐射流量调整到1个天文单位的值, FAPKP[13]
        ("Ap",          c_int * 8),     # 3小时Ap值, FAPKP[5..12]
        ("Ap_avg",      c_int),         # 当天Ap均值,FAPKP[13]
        ("Kp_avg",      c_double)       # 当天Kp均值, FAPKP[4]   
    ]

# 创建 DTIME 结构体原型
class DTIME(Structure):
    _fields_ = [
        ("EOP",     DEOP),              # EOP参数结构体
        ("JUTC",    c_double),          # 儒略日 协调世界时
        ("JTT",     c_double),          # 儒略日 动力学时 
        ("JUT1",    c_double),          # 儒略日 世界时
        ("MHPT",    c_double * 9),      # PTOD-->ECI
        ("MGQT",    c_double * 9),      # TEME-->ECI
        ("MHG",     c_double * 9),      # ECI-->ECF
        ("DHG",     c_double * 9)
    ]

# 创建 DSATEPH_CA 结构体原型, 单点卫星星历数据(ECI)
class DSATEPH_CA(Structure):
    _fields_ = [
        ("jutc",        c_double),      # 儒略日 协调世界时
        ("jtt",         c_double),      # 儒略日 动力学时
        ("jut1",        c_double),      # 儒略日 世界时
        ("vecr",        c_double * 3),  # J2000惯性系位置分量[m]
        ("vecv",        c_double * 3),  # J2000惯性系速度分量[m/s]
        # KEPLER六根数[0..5], 半长轴, 偏心率, 倾角, 升交点赤经, 近地点幅角, 平近点角
        ("ele_kpl_osc", c_double * 6),  # J2000惯性系KEPLER六根数, 瞬根[m,NA,rad/rad/rad/rad/rad]
        ("ele_kpl_avg", c_double * 6)   # J2000惯性系KEPLER六根数, 平根[m,NA,rad/rad/rad/rad/rad]
    ]

# 创建 DCA_TimeSpan 结构体原型
class DCA_TimeSpan(Structure):
    _fields_ = [
        ("satid_p",         c_int),                 # 主目标编号
        ("satid_s",         c_int),                 # 从目标编号
        ("mjds",            c_double),              # 起始时刻 约简儒略日[UTC]
        ("mjde",            c_double),              # 结束时刻
        ("drs",             c_double),              # 起始时刻相对距离[m]
        ("dre",             c_double),              # 结束时刻相对距离[m]

        # 存放处于该时间区间内的交会接近记录在全部交会接近记录数组中的位置
        # 对每一类接近(type_ca=1/0), 每天最多有16*2=32次, 预警区间最多不超过7天.
        ("n_ca_1",          c_int),                 # [mjds,mjde]时间区间内的最近接近总次数
        ("n_ca_0",          c_int),                 # [mjds,mjde]时间区间内的最近接近总次数
        ("array_pos_ca_1",  c_int * NMAX_CA_RST),   # type_ca=1
        ("array_pos_ca_0",  c_int * NMAX_CA_RST)    # type_ca=0
    ]

# 创建 DCA_Result 结构体原型, 交会接近分析输出结果
class DCA_Result(Structure):
    _fields_ = [
        ("type_ca",         c_int),             # 1-极大值,0-极小值
        ("jtt_ca",          c_double),          # 接近距离极值时刻(JTT) *
        ("mjd_ca",          c_double),          # 接近距离极值时刻(MJD)
        ("vecr_p",          c_double * 3),      # 接近距离极值时刻的 主目标 位置参数 [m]
        ("vecv_p",          c_double * 3),      # 接近距离极值时刻的 主目标 速度参数 [m/s]
        ("vecr_s",          c_double * 3),      # 接近距离极值时刻的 从目标 位置参数 [m]
        ("vecv_s",          c_double * 3),      # 接近距离极值时刻的 从目标 速度参数 [m/s]
        ("dr_ca",           c_double),          # 接近距离极值[m]
        ("dv_ca",           c_double),          # 接近距离极值时刻的接近速度[m/s]
        ("theta",           c_double),          # 接近距离极值时刻的交会角 速度矢量夹角[rad]
        ("LBH",             c_double * 3),      # 接近距离极值时刻的主目标地固系星下点位置[rad/rad/m]
        ("dt_p",            c_double),          # 主目标接近距离极值时刻与根数历元的时间间隔[day]
        ("dt_s",            c_double),          # 从目标接近距离极值时刻与根数历元的时间间隔[day]
        ("sigma_p",         c_double * 3),      # 主目标三方向的位置误差[m]
        ("sigma_s",         c_double * 3),      # 从目标三方向的位置误差[m]
        ("pc",              c_double),          # 碰撞概率

        # 主目标STW坐标系中描述
        ("dr_stw",          c_double * 3),      # 接近距离极值时刻的 相对位置分量[m]
        ("dv_stw",          c_double * 3),      # 接近距离极值时刻的 相对速度分量[m/s]

        # 主目标UNW坐标系中描述
        ("dr_unw",          c_double * 3),      # 接近距离极值时刻的 相对位置分量[m]

        ("satid_p",         c_int),             # 主目标编号
        ("satid_s",         c_int),             # 从目标编号
        ("id_sat_s",        c_int),             # 从目标在从目标数组中的序列位置索引, 取值范围[0, nsat_p-1]
    ]

# 创建 DMVPara 结构体原型, 变轨参数
class DMVPara(Structure):
    _fields_ = [ 
        ("sn_mv",           c_int),             # 当次变轨事件序号(第i个变轨), i>=0
        ("sn_father",       c_int),             # 所继承的上一变轨事件的序号, sn_mv=0时继承本身,首次变轨的sn_father=-1
        ("num_mv",          c_int),             # 变轨次数,num_mv>=1
        ("type_mv",         c_int),             # 变轨策略控制(dS/dT), 取值为 1,2,3,4
        ("jtt0",            c_double),          # 用于计算变轨时刻卫星位置速度的初始根数历元时刻
        ("ele_kpl0",        c_double * 6),      # 用于计算变轨时刻卫星位置速度的初始根数
        ("jtt",             c_double),		    # 变轨时刻
        ("vecr",            c_double * 3),      # 变轨时刻卫星惯性系位置矢量[m]
        ("vecv_minus",      c_double * 3),		# 变轨时刻卫星惯性系速度矢量[m/s], 添加速度增量前
        ("ca_event",        DCA_Result),        # 接近事件复核结果中最近一次接近事件, 以该事件为规避对象
        ("vecdv",           c_double * 3),      # 惯性系变轨速度增量[m/s]
        ("vecdv_stw",       c_double * 3),      # 卫星STW坐标系下的变轨速度增量[m/s]
        ("vecv_plus",       c_double * 3),      # 变轨时刻卫星惯性系速度矢量[m/s], 添加速度增量后.
        ("ele_kpl_plus",    c_double * 6),      # 变轨时刻卫星惯性系KEPLER根数[m/rad], 添加速度增量后
        ("vecdr_ca_stw",    c_double * 3),
        ("vecdv_ca_stw",    c_double * 3),
        ("DeltV_all",       c_double),          # 累计速度增量[m/s]
        ("flag",            c_int),             # -1标识失败,0标识继续,+1标识成功.
    ]

# 创建 DObsData 结构体原型, 变轨参数
class DObsData(Structure):
    _fields_ = [
        ("yr",              c_int),             # 测量时刻(年月日时分秒,UTC时刻)
        ("mo",              c_int),
        ("dy",              c_int),
        ("hr",              c_int),
        ("mi",              c_int),
        ("se",              c_double),
        ("mjd",             c_double),          # 约简儒略日[UTC]
        ("JTT",             c_double),          # 动力学时
        ("id_t",            c_int),             # 时标标识, 1-设备发送信号时刻, 2-信号离开目标时刻, 3-设备收到信号时刻
        ("tau",             c_double),          # 时标修正值[ms]
        ("rECI",            c_double * 3),      # 观测站ECI坐标系中的位置(适用于SBV)
        ("vECI",            c_double * 3),      # 观测站ECI坐标系中的速度(适用于SBV)
        ("ObsVal",          c_double * 2),      # 观测数据 实测值, 赤经[rad], 赤纬[rad]
        ("CalVal",          c_double * 2),      # 观测数据 计算值, 赤经[rad], 赤纬[rad]
        ("dCalVal",         c_double * 3),      # 观测数据时间变化率, 赤经变化率[rad/sec], 赤纬变化率[rad/sec], 空间角度变化率[rad/sec]
        ("ResVal",          c_double * 3),      # 观测数据残差, 赤经残差[rad], 赤纬残差[rad], 空间角度残差[rad]
    ]

# C结构体 <-> 内存字串

def LEAPS2Py( LEAPS ):
    pyLEAPS= string_at(addressof(LEAPS),sizeof(LEAPS))
    return pyLEAPS

def pyLEAPS2C( n, pyLEAPS ):
    LEAPS  = (DLEAP * n.value)()
    memmove(addressof(LEAPS), pyLEAPS, sizeof(LEAPS))
    return LEAPS

def DEOP2Py( EOPS ):
    pyEOPS = string_at(addressof(EOPS),sizeof(EOPS))
    return pyEOPS

def pyDEOP2C( n, pyEOPS ):
    EOPS = ( DEOP * n.value)()
    memmove(addressof(EOPS), pyEOPS, sizeof(EOPS))
    return EOPS

def DSWData2Py( swdata ):
    py_swdata = string_at(addressof(swdata),sizeof(swdata))
    return py_swdata

def pyDSWData2c( n, py_swdata ):
    swdata = (DSWData * n )()
    memmove(addressof(swdata), py_swdata, sizeof(swdata))
    return swdata

def pySateph2C(n, sateph_str ):
    sateph = (DSATEPH_CA * n)()
    memmove(addressof(sateph), sateph_str, sizeof(sateph))
    return sateph

# 读取跳秒文件
def loadLeap( path_leap ):

    if not os.path.exists(path_leap):
        print("ERROR: 缺少跳秒数据! 路径: ' %s ' 不存在!\n"%(path_leap) )
        return -1

    # 初始化cdll对象
    so_obj = cdll.LoadLibrary(c_lib_path)
    # 初始化全局参数+地球引力场参数(20X20)
    so_obj.dll_Initialization_Const()

    # print("loadLeap, so_obj=", so_obj)

    # 定义数据类型
    char600 = c_char * 600 

    # 读取跳秒值TAI-UTC
    N_LEAP = c_int()
    LEAPS  = (DLEAP * NMAX_LEAP)()

    fname_lep = char600()
    fname_lep = create_string_buffer(path_leap.encode(), 600)
    flag = so_obj.dll_GetLEAPData( fname_lep, pointer(N_LEAP), LEAPS)

    return N_LEAP, LEAPS

# 读取极移文件, 根据观测时间范围, 选取所需数据
def loadEOP(path_eop, jutcs, jutce ):

    if not os.path.exists(path_eop):
        print("ERROR: 缺少极移数据! 路径: ' %s ' 不存在!\n"%(path_eop) )
        return -1

    # 初始化cdll对象
    so_obj = cdll.LoadLibrary(c_lib_path)
    # 初始化全局参数+地球引力场参数(20X20)
    so_obj.dll_Initialization_Const()

    # 读取jutc时间范围
    # jutcs = obsTimeObj['jutcs']
    # jutce = obsTimeObj['jutce']

    # 确定预报区间 中间点时间
    yr = c_int()
    mo = c_int()
    dy = c_int()
    hr = c_int()
    mi = c_int()
    se = c_double()

    so_obj.dll_JulianDate2YMDHMS( c_double( 0.5*(jutcs + jutce) ), pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se) )

    # 导入EOP参数
    N_EOP  = c_int()
    EOPS   = (DEOP * ( HalfLen_EOP * 2 + 1 ))()

    # 定义数据类型
    char600 = c_char * 600
    fname_eop = char600()
    fname_eop = create_string_buffer(path_eop.encode(), 600)

    flag = so_obj.dll_InitializeEOPArray( fname_eop, yr, mo, dy, 10, pointer(N_EOP), EOPS )

    return N_EOP, EOPS

# 读取极移文件, 根据观测时间范围, 选取所需数据
def loadSpaceWeather(path_swd, jutcs, jutce ):

    if not os.path.exists(path_swd):
        print("ERROR: 缺少空间环境数据! 路径: ' %s ' 不存在!\n"%(path_swd) )
        return -1

    # 初始化cdll对象
    so_obj = cdll.LoadLibrary(c_lib_path)
    # 初始化全局参数+地球引力场参数(20X20)
    so_obj.dll_Initialization_Const()

    # 初始化变量
    swdata = (DSWData * (HalfLen_Flux * 2 + 1) )()
    
    # 定义数据类型
    char600 = c_char * 600
    fname_swd = char600()
    fname_swd = create_string_buffer(path_swd.encode(), 600)

    # 读取jutc时间范围
    # jutcs = obsTimeObj['jutcs']
    # jutce = obsTimeObj['jutce']

    # 导入SPACEWEATHER参数
    flag = so_obj.dll_GetSpaceWeatherData( fname_swd, c_double( 0.5*(jutcs + jutce) ), 10, swdata )

    return swdata

# 读取轨道预报误差文件
def loadOrbitPropError( path_syserr ):
    if not os.path.exists(path_syserr):
        print("ERROR: 缺少轨道预报误差数据文件! 路径: ' %s ' 不存在!\n"%(path_syserr) )
        return -1
    
    fh = open(path_syserr, 'r', encoding='utf-8')
    lines = fh.readlines()
    fh.close()

    line_id = 0
    re = []

    for oneline in lines:
        line_nosp = ' '.join(oneline.split())   # 移除多余空格
        # print(line_nosp[0])
        if not ( line_nosp[0] == 'C' ):
            line_sp = line_nosp.split(' ')

            ds = float (line_sp[1])
            dt = float (line_sp[2])
            dw = float (line_sp[3])

            ks = float (line_sp[4])
            kt = float (line_sp[5])
            kw = float (line_sp[6])

            if line_id == 0:
                re.append( { 'ds0':ds, 'dt0':dt, 'dw0':dw, 'ks':ks, 'kt':kt, 'kw':kw } )

    return re

# 解析订单的观测开始时间、观测持续时间字串，求取儒略历起止时间
def getStartTime( time_start_utc, time_span ):

    # 初始化cdll对象
    so_obj = cdll.LoadLibrary(c_lib_path)
    # 初始化全局参数+地球引力场参数(20X20)
    so_obj.dll_Initialization_Const()

    so_obj.dll_YMDHMS2JulianDate.restype = c_double

    # 拆解开始时间 格式: yr-mo-dy hr:mi:se
    t = time_start_utc.split(' ')
    [yr, mo, dy] = t[0].split('-')
    [hr, mi, se] = t[1].split(':')

    yr = int(yr)
    mo = int(mo)
    dy = int(dy)
    hr = int(hr)
    mi = int(mi)
    se = 0.0

    # print( yr, mo, dy, hr, mi, se )
    dt = float( time_span )

    if dt < 1.0 :
        dt = 1.0
    
    if dt > 7.0:
        dt = 7.0

    # 预报起止时间
    # 预报起始时刻[UTC]
    jutcs = so_obj.dll_YMDHMS2JulianDate(c_int(yr), c_int(mo), c_int(dy), c_int(hr), c_int(mi), c_double(se) )
    # 预报结束时刻[UTC]
    jutce = jutcs + dt

    #定义预报时间 MJD
    mjds = jutcs - 2400000.5
    mjde = jutce - 2400000.5

    # 返回时间对象
    return { 'yr':yr, 'mo':mo, 'dy':dy, 'hr':hr, 'mi':mi, 'se':se , 
             'dt':dt,
             'jutcs':jutcs,
             'jutce':jutce,
             'mjds':mjds,
             'mjde':mjde
             }

# 读取目标文件, 返回3行轨道数据
def loadSatOrbitFile( path_sat ):

    if not os.path.exists(path_sat):
        print("ERROR: 缺少卫星轨道数据! 路径: ' %s ' 不存在!\n"%(path_sat) )
        return -1, {}
    
    # 打开文件
    fh = open(path_sat, 'r')
    # 读取文件
    lines = fh.readlines()
    # 关闭文件
    fh.close()

    # 初始化返回值
    re = []
    list3line = []
    # 逐行解析文件
    for oneline in lines:
        # print(oneline)
        # line_nosp = ' '.join(oneline.split())   # 移除多余空格
        line_nosp = oneline
        if not line_nosp == '':
            if line_nosp[0] == '0':
                list3line = []
                list3line.append(line_nosp)
                re.append(list3line)
            else:
                list3line.append(line_nosp)
            # print(re)
            
    return 0, re

# 解析自定义的3行轨道数据
def decode3LinesOrbitParam( lines ):
    # print(lines)
    # 初始化参数
    ele_shema = 0                       # 初始化输入格式
    type_ele = -1                       # 轨道根数类型 0-历元+惯性系位置速度+面质比参数 1-历元+惯性系KEPLER根数+面质比参数

    satid = 0                           # 目标id *
    raddi = 0                           # 目标等效半径 *
    AM0 = 0.005                         # 目标面质比 [m^2/kg], 默认0.005 *

    # 历元
    yr = 0
    mo = 0
    dy = 0
    hr = 0
    mi = 0
    se = 0.0

    # 惯性系位置
    vr_x = 0.0
    vr_y = 0.0
    vr_z = 0.0

    # 惯性系速度
    vv_x = 0.0
    vv_y = 0.0
    vv_z = 0.0

    # 双行根数字符串
    tle_line_1 = ""
    tle_line_2 = ""

    # 判断文件完整性


    # 解析内容
    for oneline in lines:
        # print(oneline)
        if oneline[0] == '0':
            # print(oneline)
            line_sp = ( ' '.join(oneline.split()) ).split(' ')
            # 获得输入格式 
            ele_shema = int(line_sp[1])     # 取值范围: 1-初轨 2-tle双行根数 3-精轨
            # 获得目标等效半径
            raddi = float(line_sp[2])       

        elif oneline[0] == '1':
            # print(oneline)
            if 2 == ele_shema:          # 2-tle双行根数
                tle_line_1 = oneline
            else:                       # 1-初轨 3-精轨 处理方法相同
                line_sp = ( ' '.join(oneline.split()) ).split(' ')
                satid   = int( line_sp[1] )
                AM0   = float( line_sp[2] )
                yr  = int( line_sp[3] )
                mo  = int( line_sp[4] )
                dy  = int( line_sp[5] )
                hr  = int( line_sp[6] )
                mi  = int( line_sp[7] )
                se  = float( line_sp[8] )

        elif oneline[0] == '2':
            # print(oneline)
            if 2 == ele_shema:          # 2-tle双行根数
                tle_line_2 = oneline
            else:                       # 1-初轨 3-精轨 处理方法相同
                line_sp = ( ' '.join(oneline.split()) ).split(' ')
                type_ele = int( line_sp[1] )
                vr_x = float( line_sp[2] )
                vr_y = float( line_sp[3] )
                vr_z = float( line_sp[4] )
                vv_x = float( line_sp[5] )
                vv_y = float( line_sp[6] )
                vv_z = float( line_sp[7] )

    # 构建返回值
    re= {
        'ele_shema':ele_shema,
        'type_ele':type_ele,
        'satid':satid,
        'AM0':AM0,
        'raddi':raddi,
        'time':{ 'yr':yr, 'mo':mo, 'dy':dy, 'hr':hr, 'mi':mi, 'se':se },
        'vr':[ vr_x, vr_y, vr_z ],
        'vv':[ vv_x, vv_y, vv_z ],
        'tle_L1':tle_line_1,
        'tle_L2':tle_line_2
    }

    return re

# 创建时间表数组
def getArrayTT(
    N_LEAP, LEAPS, N_EOP, EOPS,
    obsTimeObj,
    lth_step
):
    #----------------------------------------------------------------
    # 初始化cdll对象
    so_obj = cdll.LoadLibrary(c_lib_path)

    # 初始化全局参数+地球引力场参数(20X20)
    so_obj.dll_Initialization_Const()
    so_obj.dll_InitializeTIMEArray.restype = c_int

    # 预报步长
    step = lth_step / 86400.0	#预报点输出步长[day]

    # 准备预报时间参数
    dt = obsTimeObj['dt']
    mjds = obsTimeObj['mjds']
    mjde = obsTimeObj['mjde']

    #初始化预报点时间数组
    pyNMAX_EPK = (int)(dt / step) + 100
    NMAX_EPK = c_int(pyNMAX_EPK)

    Array_TT = (DTIME * pyNMAX_EPK)()

    # 获得预报时间点数组 及 时间点个数
    NEPK = so_obj.dll_InitializeTIMEArray(
        N_LEAP,
        LEAPS,
        N_EOP,
        EOPS,
        c_double(mjds),
        c_double(mjde),
        c_double(step),
        NMAX_EPK,
        pointer(Array_TT)
    )                       # NEPK 星历数据点数

    return NEPK, Array_TT

# 轨道外推
def orbitProp(
    N_LEAP, LEAPS, N_EOP, EOPS, swdata,     # 公共环境参数
    NEPK, Array_TT,                         # 时间参数
    satInfo                                 # 轨道参数
    ):
    
    #----------------------------------------------------------------
    # 初始化cdll对象
    so_obj = cdll.LoadLibrary(c_lib_path)

    # 初始化全局参数+地球引力场参数(20X20)
    so_obj.dll_Initialization_Const()

    so_obj.dll_CalJTT.restype = c_double
    so_obj.dll_GetTleEpoch.restype = c_double
    so_obj.dll_YMDHMS2JulianDate.restype = c_double

    #----------------------------------------------------------------

    satid = satInfo['satid']
    raddi = satInfo['raddi']
    # 轨道根数类型
    ele_shema = satInfo['ele_shema']
    type_ele = satInfo['type_ele']
    # type_ele = 0

    # 面质比
    AM0 = satInfo['AM0']

    jtt_epk = 0.0                       # 动力学时间 *
    sateph = (DSATEPH_CA * NEPK)()      # 轨道外推结果 *

    #----------------------------------------------------------------
    # 获得轨道参数
    
    # 外推目标轨道根数
    if ele_shema == 2:    # TLE双行根数
        # TLE 双行根数数据
        tle_line_1 = satInfo['tle_L1']
        tle_line_2 = satInfo['tle_L2']

        line1 = create_string_buffer(tle_line_1.encode(), LTH_TLE)
        line2 = create_string_buffer(tle_line_2.encode(), LTH_TLE)
        
        # 读取目标ID
        satid = int( tle_line_1[2:7] )

        # 计算动力学时间
        # temp = so_obj.dll_GetTleEpoch( line1 )
        jtt_epk = so_obj.dll_CalJTT(
            c_double(so_obj.dll_GetTleEpoch( line1 )),
            # c_double(temp),
            N_LEAP,
            LEAPS )

        # TLE轨道外推预报
        flag = so_obj.dll_PropOrbit_Tle(
            N_LEAP, LEAPS,
            line1, line2,
            NEPK,
            pointer(Array_TT),
            pointer(sateph) )

    else:   # 1-初轨 3-精轨 处理方法相同

        # 注意: 这里是目标的星历时间, 不要与观测时间混淆
        t = satInfo['time']             
        yr = t['yr']
        mo = t['mo']
        dy = t['dy']
        hr = t['hr']
        mi = t['mi']
        se = t['se']

        # 位置 速度
        vr = satInfo['vr']
        vv = satInfo['vv']

        # 计算动力学时间
        jutc = so_obj.dll_YMDHMS2JulianDate(c_int(yr), c_int(mo), c_int(dy), c_int(hr), c_int(mi), c_double(se) )
        mjd0 = jutc - 2400000.5
        jtt_epk = so_obj.dll_CalJTT( c_double(jutc) ,N_LEAP, LEAPS )

        # 初始化KEPLER六根数[a/m,ecc/nan,incl-rad,RAAN-rad,w-rad,M-rad]
        ele_kpl0_p = (c_double * 6)()

        # 判断轨道根数类型
        if type_ele == 0:

            # 惯性系位置
            vr0 = (c_double * 3)( vr[0], vr[1], vr[2] )
            # 惯性系速度
            vv0 = (c_double * 3)( vv[0], vv[1], vv[2] )

            # 位置速度-->轨道根数(kepler)
            so_obj.dll_State_Transform_PosVel2ELE_kpl( vr0, vv0, ele_kpl0_p )

        elif type_ele == 1:

            # 惯性系位置速度
            data_ele = (c_double * 6)( vr[0], vr[1], vr[2], vv[0], vv[1], vv[2] )
        
            # 单位转换
            so_obj.dll_UnitTransform_Orbit_Elements(0, data_ele, ele_kpl0_p)
       
        # 利用主目标(卫星)KEPLER根数进行轨道外推
        flag = so_obj.dll_PropOrbit_Kepler(
            N_LEAP, LEAPS,
            N_EOP,  EOPS,
            swdata,
            NEPK,
            pointer(Array_TT),
            c_double(mjd0),
            c_double(jtt_epk),
            ele_kpl0_p,
            c_double(AM0),
            0,
            pointer(sateph) )
        
    
    re = {
        'satid':satid,
        'raddi':raddi,
        'jtt_epk':jtt_epk,
        'ele_shema':ele_shema,
        'AM0':AM0,
        'sateph':sateph,
        # 'sateph_str': string_at(addressof(sateph),sizeof(sateph))
    }

    return re

# 轨道碰撞筛查
def Filter_CloseApproach(
    N_LEAP, LEAPS, N_EOP, EOPS, NEPK, Array_TT,
    sat_p, sat_s,
    gate_dr, lth_step, dt, sys_error_table ):

    #----------------------------------------------------------------
    # 初始化cdll对象
    so_obj = cdll.LoadLibrary(c_lib_path)

    # 初始化全局参数+地球引力场参数(20X20)
    so_obj.dll_Initialization_Const()

    #----------------------------------------------------------------
    # 确定门限
    gate_dr_ca = min( 100E+3, 2.0*gate_dr )

    gH = max(30000.0, 2.0*gate_dr_ca)
    gD0 = max(150000.0, 5.0*gate_dr_ca)			# [m],最近轨道距离筛选门限

    #----------------------------------------------------------------
    # 主目标参数
    satid_p = sat_p['satid']
    satephs_p = sat_p['sateph']
    jtt_epk_p = sat_p['jtt_epk']
    AM0_p = sat_p['AM0']
    raddi_p = sat_p['raddi']

    # 从目标参数
    satid_s = sat_s['satid']
    satephs_s = sat_s['sateph']
    jtt_epk_s = sat_s['jtt_epk']
    AM0_s = sat_s['AM0']
    raddi_s = sat_s['raddi']

    # ----------------------------------------------------------------------------------
    # 输出主目标/从目标轨道位置数据

    # print("jtt_epk_p =", jtt_epk_p)
    # print("jtt_epk_s =", jtt_epk_s)
    
    # r_rel = (c_double*3)()
    # v_rel = (c_double*3)()
    # sun_angle = c_double()

    # xn_avg = 0.0
    # for i in range(0, NEPK):
    #     xn_avg += sqrt(MU / satephs_p[i].ele_kpl_avg[0] / satephs_p[i].ele_kpl_avg[0] / satephs_p[i].ele_kpl_avg[0])
    # xn_avg = xn_avg / NEPK

    # print("xn_avg = %8.6f"%(xn_avg) )

    # for i in range(0, NEPK):
    #     so_obj.dll_CalRV_RefFrame(
    #         satephs_p[i].vecr, satephs_p[i].vecv,
    #         satephs_s[i].vecr, satephs_s[i].vecv,
    #         r_rel, v_rel )

    #     sun_angle = so_obj.dll_CalAngle_SunPhase(
    #         c_double(satephs_p[i].jtt),
    #         satephs_p[i].vecr,
    #         satephs_s[i].vecr )

    #     xc0 = 4.0*r_rel[0] + 2.0*v_rel[1] / xn_avg
    #     yc0 = r_rel[1] - 2.0*v_rel[0] / xn_avg

    #     a1 = (v_rel[0] / xn_avg)
    #     a2 = (3.0*r_rel[0] + 2.0*v_rel[1] / xn_avg)
    #     a = sqrt( a1 * a1 + a2 * a2)

    #     # print("i=%d, a=%f"%(i, a))

    #     dyc0 = -1.5*xc0*xn_avg*86400.0

    #     # 确定预报区间 中间点时间
    #     yr = c_int()
    #     mo = c_int()
    #     dy = c_int()
    #     hr = c_int()
    #     mi = c_int()
    #     se = c_double()

    #     so_obj.dll_JulianDate2YMDHMS(
    #         c_double( Array_TT[i].JUTC ),
    #         pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se) )

    #     print("  %04d  %02d  %02d  %02d  %02d  %09.6f"%(
    #         yr.value, mo.value, dy.value, hr.value, mi.value, se.value),
    #         end="")
    #     print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%(
    #         satephs_p[i].vecr[0], satephs_p[i].vecr[1], satephs_p[i].vecr[2],
    #         satephs_p[i].vecv[0], satephs_p[i].vecv[1], satephs_p[i].vecv[2]),
    #         end="")
    #     print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%(
    #         satephs_s[i].vecr[0], satephs_s[i].vecr[1], satephs_s[i].vecr[2],
    #         satephs_s[i].vecv[0], satephs_s[i].vecv[1], satephs_s[i].vecv[2]),
    #         end="")
    #     print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%(
    #         r_rel[0], r_rel[1], r_rel[2], v_rel[0], v_rel[1], v_rel[2]),
    #         end="")
    #     print("  %15.3f"%(sqrt(r_rel[0] * r_rel[0] + r_rel[1] * r_rel[1] + r_rel[2] * r_rel[2])),
    #         end="")
    #     print("  %15.6f"%(sqrt(v_rel[0] * v_rel[0] + v_rel[1] * v_rel[1] + v_rel[2] * v_rel[2])),
    #         end="")
    #     print("  %8.4f"%(sun_angle * Rad2Deg) )

    #     print("  %15.3f  %15.3f  %15.3f  %15.3f"%(xc0, yc0, a, dyc0) )
    
    # ----------------------------------------------------------------------------------
    # 1.主-从目标交会接近筛查: 轨道高度+轨道间最近距离
    f_filter = so_obj.dll_Filter_CloseApproach(
        # 公共参数
        N_LEAP, LEAPS,
        N_EOP, EOPS,
        NEPK,                   # 星历数据点数

        c_double(lth_step),     # 星历输出步长[sec]

        satid_p,                # 主目标ID
        satephs_p,              # 主目标轨道外推
        c_double(jtt_epk_p),    # 主目标轨道外推 动力学时间
        c_double(AM0_p),        # 主目标面质比

        satid_s,                # 从目标ID
        satephs_s,              # 从目标轨道外推
        c_double(jtt_epk_s),    # 从目标轨道外推 动力学时间
        c_double(AM0_s),        # 从目标面质比

		c_double(gH),           # 轨道高度差筛查门限[m]
        c_double(gD0)           # 轨道间最近距离筛查门限[m]
    )
    
    # 无交会接近
    if f_filter < 0:
        return 0

    # 2.计算交会接近参数

    # 初始化参数
    n = ceil(dt * 80)
    ca_ts = (DCA_TimeSpan * n)()
    ca_rst = (DCA_Result * n)()

    nstep = 1
    n_ca_ts = c_int()

    # 计算接近事件(主目标星历 Vs. 从目标星历)
    n_ca_rst = so_obj.dll_FindCloseApproachEvent_eph_vs_eph(
        N_LEAP, LEAPS,
        N_EOP, EOPS,
		NEPK,                   # 星历数据点数
        c_double(lth_step),     # 星历外推步长, 即相邻两组星历的历元时间间隔[sec]
		
        satid_p,                # 主目标ID
        c_double(jtt_epk_p),    # 主目标轨道外推 动力学时间
        satephs_p,              # 主目标轨道外推
		
        satid_s,                # 从目标ID
        c_double(jtt_epk_s),    # 从目标轨道外推 动力学时间
        satephs_s,              # 从目标轨道外推
		
        0, NEPK - 1,            # 需要计算的星历位置区间[pos_s,pos_e],为[0,neph-1]的子集
        c_int(nstep),           # 星历数据步进长度(一次步进几组数据,一般nstep=1)
        c_double(gate_dr_ca),   # 接近距离门限[m]
                                # 用于计算相对距离小于该门限的时间区间, 并筛查最近接近距离小于该门限的最近接近事件
        # 输出
        pointer(n_ca_ts),       # 接近事件时间区间个数
        ca_ts,                  # 接近事件时间区间数组

        ca_rst                  # 交会接近分析输出结果数组
    )                           # n_ca_rst, 交会接近分析输出结果个数
    
    # print("n_ca_rst =", n_ca_rst)
    # print("n_ca_ts =", n_ca_ts.value)

    # for i in range(n_ca_rst):
        # print( i, ca_rst[i].dr_stw[0], ca_rst[i].dr_stw[1], ca_rst[i].dr_stw[2], ca_rst[i].dr_ca)

    # 3.计算碰撞概率
    ellipsoid_type = 1          # 误差椭球坐标类型标识(1-STW, 2-UNW)

    # 主目标轨道外推误差
    error_p = sys_error_table[0]

    # 从目标轨道外推误差
    ele_shema = sat_s['ele_shema']
    error_s = sys_error_table[ele_shema]

    # print("error_p", error_p)
    # print("error_s", error_s)

    for i in range(0, n_ca_rst):
        
        # 不计算极大值时的碰撞概率
        if (ca_rst[i].type_ca == 1):
            continue

        # 计算预报误差
        ca_rst[i].sigma_p[0] = ca_rst[i].dt_p * error_p['ks'] + error_p['ds0']
        ca_rst[i].sigma_p[1] = ca_rst[i].dt_p * error_p['kt'] + error_p['dt0']
        ca_rst[i].sigma_p[2] = ca_rst[i].dt_p * error_p['kw'] + error_p['dw0']

        ca_rst[i].sigma_s[0] = ca_rst[i].dt_s * error_s['ks'] + error_s['ds0']
        ca_rst[i].sigma_s[1] = ca_rst[i].dt_s * error_s['ks'] + error_s['ds0']
        ca_rst[i].sigma_s[2] = ca_rst[i].dt_s * error_s['ks'] + error_s['ds0']

        # 利用最近接近时的主目标、从目标位置、速度、位置误差协方差、半径等参数，计算碰撞概率
        flag_pc = so_obj.dll_CalCollisionProbability(
            ca_rst[i].vecr_p,       # 主目标惯性系 位置矢量 [m]
            ca_rst[i].vecv_p,       # 主目标惯性系 速度矢量 [m/s]
            ca_rst[i].sigma_p,      # 主目标三方向的位置误差 [m]
            c_double(raddi_p),      # 主目标等效半径 [m] 
            
            ca_rst[i].vecr_s,       # 从目标惯性系 位置矢量 [m]
            ca_rst[i].vecv_s,       # 从目标惯性系 速度矢量 [m/s]
            ca_rst[i].sigma_s,      # 从目标三方向的位置误差 [m]
            c_double(raddi_s),      # 从目标等效半径 [m]
            
            ellipsoid_type,                     # 误差椭球坐标类型标识(1-STW, 2-UNW)
            
            pointer(c_double(ca_rst[i].pc))     # 该次最近接近事件的碰撞概率，取值范围[0.0, 1.0]
        )                                       # flag_pc: 0-正常返回; <0-异常返回

        # print("i = %d, flag_pc = %d"%(i, flag_pc) )

        if ca_rst[i].pc < 1.0e-100:
            ca_rst[i].pc = 0.0

    # print('len(ca_rst) = ', len(ca_rst) )
    # print('n_ca_rst = ', n_ca_rst)
    # re = {
    #     'sat':sat_s,
    #     'ca_rst':ca_rst,
    #     'n_ca_rst':n_ca_rst
    # }

    return n_ca_rst, ca_rst

# 判断风险等级
# 风险等级 0-无风险 1-一般风险 2-引起注意 3-需要处理
def getAlarmLevel_OrbitCA(
    one_ca_rst,
    start_jtt,          #   ArrayTT[0].jtt
    gate_dr, gate_dSTW, gate_etca, gate_pc
):
    # 初始化
    alarm_dswt = 0   # dsw三轴距离告警
    alarm_pc = 0    # 碰撞概率告警
    alarm_dr = 0    # 直线距离告警

    # 读取接近事件发生时间
    alarm_jtt_ca = one_ca_rst.jtt_ca

    # --------------------------------------------------
    # 计算预计还有多少时间发生接近事件
    etca = alarm_jtt_ca - start_jtt

    # --------------------------------------------------
    # 基与目标间距判断告警等级

    if ( ( abs( one_ca_rst.dr_stw[0] ) <  gate_dSTW[0] ) and
        ( abs( one_ca_rst.dr_stw[1] ) <  gate_dSTW[1] ) and
        ( abs( one_ca_rst.dr_stw[2] ) <  gate_dSTW[2] ) ):
        
        if etca > gate_etca:
            alarm_dswt = 2           # 距离较近 但 时间不紧迫: 风险等级：2-引起注意
        else:
            alarm_dswt = 3           # 距离较近 且 时间紧迫: 风险等级：3-需要处理

    # --------------------------------------------------
    # 基与碰撞概率判断告警等级
    if one_ca_rst.pc >= gate_pc:    # 概率较高

        if etca > gate_etca:
            alarm_pc = 2            # 概率较高 但 时间不紧迫: 风险等级：2-引起注意
        else:
            alarm_pc = 3            # 概率较高 且 时间紧迫: 风险等级：3-需要处理

    # --------------------------------------------------
    # again 基与目标间距判断告警等级
    if one_ca_rst.dr_ca <= gate_dr: # 距离进入直线距离门限
        alarm_dr = 1                # 风险等级：1-一般

    # 构造返回对象
    alarmObj = {
        'satid_s':one_ca_rst.satid_s,
        'max': max( alarm_dswt, alarm_pc, alarm_dr),
        'jtt_ca':alarm_jtt_ca,
        'dswt':alarm_dswt,
        'dr':alarm_dr,
        'pc':alarm_pc
    }
    
    return alarmObj

def LoadAERData(N_LEAP, LEAPS, 
    fin_aer,
    n_skip, n_max,
    array_utc, array_tt,
    array_azi, array_elv, array_rho,
    array_r_tgt
    ):

    if not os.path.exists(fin_aer):
        print("ERROR: 路径: ' %s ' 不存在!\n"%(fin_aer) )
        return -1

    #----------------------------------------------------------------
    # 初始化cdll对象
    so_obj = cdll.LoadLibrary(c_lib_path)

    # 初始化全局参数+地球引力场参数(20X20)
    so_obj.dll_Initialization_Const()

    so_obj.dll_YMDHMS2JulianDate.restype = c_double
    so_obj.dll_CalJTT.restype = c_double

    # 打开文件
    fh = open(fin_aer, 'r')
    # 读全部行
    lines = fh.readlines()
    # 关闭文件handler
    fh.close()

    # 初始化行数计数
    lines_n = 0
    # 初始化已读行数计数
    n_data = 0
    # 逐行读取
    for oneline in lines:
        # 跳过注释行
        if oneline[0].capitalize != 'C':
            # 行数自增
            lines_n += 1
            # 跳过前n_skip行
            if ( lines_n > n_skip ):
                # 解析行
                line_sp = ( ' '.join(oneline.split()) ).split(' ')
                # print( lines_n, n_data, line_sp )
                yr = c_int( int(line_sp[0] ) )
                mo = c_int( int(line_sp[1] ) )
                dy = c_int( int(line_sp[2] ) )
                hr = c_int( int(line_sp[3] ) )
                mi = c_int( int(line_sp[4] ) )
                se = c_double( float(line_sp[5] ) )

                jutc = so_obj.dll_YMDHMS2JulianDate( yr, mo, dy, hr, mi, se )
                jtt = so_obj.dll_CalJTT(c_double(jutc), N_LEAP, LEAPS )

                # print("\n jutc = %f, jtt = %f"%(jutc, jtt))
                array_utc[n_data] = c_double( jutc )
                array_tt[n_data]  = c_double( jtt )

                # 增加仿真误差
                rnd_azi = (random() - 0.5)*0.05     #仿真测量误差[degree]
                rnd_elv = (random() - 0.5)*0.05     #仿真测量误差[degree]
                rnd_rho = (random() - 0.5)*10.0     #仿真测量误差[m]

                # print("\n rnd_azi = %f, rnd_elv = %f, rnd_rho = %f"%(rnd_azi, rnd_elv, rnd_rho) )
                azi = float( line_sp[6] )
                elv = float( line_sp[7] )
                rho = float( line_sp[8] )

                array_azi[n_data] = c_double( azi*Deg2Rad + rnd_azi*Deg2Rad )   #[rad]
                array_elv[n_data] = c_double( elv*Deg2Rad + rnd_elv*Deg2Rad )   #[rad]
                array_rho[n_data] = c_double( rho + rnd_rho )                   #[m]

                # 卫星J2000惯性系位置分量
                rx = float( line_sp[9] )
                ry = float( line_sp[10] )
                rz = float( line_sp[11] )

                array_r_tgt[n_data * 3 + 0] = c_double( rx )    #卫星位置x[m]
                array_r_tgt[n_data * 3 + 1] = c_double( ry )    #卫星位置y[m]
                array_r_tgt[n_data * 3 + 2] = c_double( rz )    #卫星位置z[m]

                # 已读行数自增
                n_data += 1

            # 满足读取数据最大行数后，退出 ??
            if n_data > n_max:
                break
    
    # 返回值
    return n_data

def Call_RadarAlarm(
    N_LEAP, LEAPS, N_EOP, EOPS,
    fname_aer,
):

   return 0

def getAvoidanceStrategy( 
    N_LEAP, LEAPS, N_EOP, EOPS, swdata, NEPK, Array_TT,
    coeff, gate_dr, py_gate_dSTW,
    gate_deltT, gate_deltV, lambda_, eps, NREV_S, NREV_T, scale,
    lth_step,
    path_mv, 
    key_ca_rst, JTT_ref,
    sat_p, sat_s_list,
):
    #----------------------------------------------------------------
    # 初始化cdll对象
    so_obj = cdll.LoadLibrary(c_lib_path)

    # 初始化全局参数+地球引力场参数(20X20)
    so_obj.dll_Initialization_Const()

    so_obj.GetCollisionAvoidanceStrategy.restype = c_int
    so_obj.dll_JTT2JUTC.restype = c_double

    # 危险接近事件筛查门限[m]
    # 当实际STW距离分量中的任一分量超出相应的门限值,认为不存在碰撞接近危险
    gate_dSTW = (c_double * 3)( py_gate_dSTW[0], py_gate_dSTW[1], py_gate_dSTW[2] )

    gate_dS = (c_double)( coeff * gate_dSTW[0] )
    gate_dT = (c_double)( coeff * gate_dSTW[1] )

    # 输出文件名
        # 定义数据类型
    char600 = c_char * 600
        # 建立C格式文件名
    fname_mv = char600()
    fname_mv = create_string_buffer(path_mv.encode(), 600)

    # 读取参数
    # 主目标参数
    satid_p = sat_p['satid']
    satephs_p = sat_p['sateph']
    AM0_p = sat_p['AM0']

    # 从目标参数
    nsat_s = len(sat_s_list)

    # 合并从目标 初始化
    Array_satid_s = (c_int * nsat_s)()
    Array_JTT_epk_s = (c_double * nsat_s)()
    Array_EPH_s = (DSATEPH_CA * (NEPK * nsat_s ))()

    # 合并从目标
    for i in range( nsat_s ):
        # 当前从目标
        sat_s = sat_s_list[i]

        satid_s = sat_s['satid']
        satephs_s = sat_s['sateph']
        jtt_ekp_s = satephs_s[0].jtt

        Array_satid_s[i] = c_int( satid_s )
        Array_JTT_epk_s[i] = jtt_ekp_s

        for j in range ( 0, NEPK ):
            Array_EPH_s[i*NEPK + j] = satephs_s[j]

    # 变轨事件记录
    Array_MVP = (DMVPara * 20 )()
    
    #
    # print("jtt", satephs_p[0].jtt)
    # a = satephs_p[0].ele_kpl_osc
    # print("ele_kpl_osc", a[0], a[1], a[2], a[3], a[4], a[5] )
    # print("key_ca_rst", key_ca_rst.jtt_ca)
    # print("JTT_ref", JTT_ref)
    # print("NREV_S", NREV_S)
    # print("NREV_T", NREV_T)
    # print("gate_dS", gate_dS)
    # print("gate_dT", gate_dT)
    # print("gate_dr", gate_dr)
    # print("gate_dSTW", gate_dSTW[0], gate_dSTW[1], gate_dSTW[2])
    # print("gate_deltT", gate_deltT)
    # print("gate_deltV", gate_deltV)
    # print("lambda_", lambda_)
    # print("eps", eps)
    # print("scale", scale)
    
    # 寻找规避策略
    num_mv = 0

    num_mv = so_obj.GetCollisionAvoidanceStrategy(
        # 公共参数
        N_LEAP, LEAPS,
        N_EOP, EOPS,
        swdata,
        # 主目标
        c_int(satid_p),             # 主目标(卫星)编号
        c_double(satephs_p[0].jtt), # 主目标(卫星)历元时刻
        satephs_p[0].ele_kpl_osc,   # 主目标(卫星)历元时刻的惯性系KEPLER根数[m/NaN/rad/rad/rad/rad]
        c_double(AM0_p),            # 主目标(卫星)面质比参数[m^2/kg]
        # 从目标
        c_int(nsat_s),              # 从目标数量
        Array_satid_s,              # 从目标ID 数组
        Array_JTT_epk_s,            # 从目标轨道根数历元 数组
        
        c_int(NEPK),                # 单个从目标星历数据点数
        Array_TT,                   # 从目标星历数据对应的历元时刻
        Array_EPH_s,                # 从目标的星历数据
        # 控制参数
        c_double(lth_step),         # 星历外推步长, 即相邻两组星历的历元时间间隔[sec]
        key_ca_rst,                 # 初始需要规避的目的接近事件[DCA_Result]
        c_double(JTT_ref),          # 首次变轨的参考时间
        c_int(NREV_S),              # 径向规避策略变轨时间控制; 以目的接近事件时刻为参考, 提前NREV_S+0.5圈进行轨控
        c_int(NREV_T),              # 迹向规避策略变轨时间控制; 以目的接近事件时刻为参考, 提前NREV_T+1.0圈进行轨控
        # 门限
        gate_dS,                    # 径向距离分量规避门限[m]
        gate_dT,                    # 迹向距离分量规避门限[m]
        c_double(gate_dr),          # 接近事件筛查距离门限[m] 
        gate_dSTW,                  # 危险接近事件筛查门限[m]. 当实际STW距离分量中的任一分量超出相应的门限值,认为不存在碰撞接近危险
        c_double(gate_deltT),       # 规避时间裕量门限[day]. 对应于相邻两次变轨的最小时间间隔; 若当前待规避危险接近事件与上次规避危险接近事件的时间间隔小于该门限值,认为规避策略失败
        c_double(gate_deltV),       # 规避速度增量门限[m/s]. 当多个规避的速度增量总和超出该门限值, 认为规避策略失败
        # 内控参数
        c_double(lambda_),          # 迭代改进控制系数[无量纲]
        c_double(eps),              # 迭代改进收敛控制参数[m]
        c_double(scale),            # 无量纲系数,取值范围[0..1],优选规避策略用
        c_int(0),                   # 输出控制,1-输出调试信息
        fname_mv,                   # 规避方案输出文件名
        Array_MVP                   # 输出: 变轨事件记录
    )                               # num_mv为机动次数(函数返回值)
                                    # >0 : 正常退出,机动规避次数
                                    # -1 : 规避策略制定失败
                                    # -2 : 首次规避时间裕量不足

    # for i in range(num_mv):
    #     print(i, Array_MVP[i].jtt0, Array_MVP[i].jtt, Array_MVP[i].vecdv_stw[1], Array_MVP[i].DeltV_all )
    # print("num_mv = ", num_mv)
    reObj = {
        'moves_count':num_mv
    }
    if num_mv <= 0 :    
        print("未能寻找到合适的规避策略!\n")

    else:
        yr = c_int()
        mo = c_int()
        dy = c_int()
        hr = c_int()
        mi = c_int()
        se = c_double()
        # print("\n\n  规避策略: 变轨次数 = %2d, 总速度变量 = %8.4f\n"%( num_mv, Array_MVP[num_mv - 1].DeltV_all) )
        mvList = []
        for j in range (0, num_mv):
            # print("j=", j)
            # print("Array_MVP[j].jtt=", Array_MVP[j].jtt )
            so_obj.dll_JulianDate2YMDHMS(
                c_double(
                    so_obj.dll_JTT2JUTC(
                        c_double(Array_MVP[j].jtt),
                        N_LEAP, LEAPS
                )),
                pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se)
            )

            # print("    第%2d次变轨:  %04d-%02d-%02d %02d:%02d:%07.4f"%(
            #     j+1, yr.value, mo.value, dy.value, hr.value, mi.value, se.value),
            #     end="")
            
            # print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%(
            #     Array_MVP[j].vecr[0], Array_MVP[j].vecr[1], Array_MVP[j].vecr[2],
            #     Array_MVP[j].vecv_minus[0], Array_MVP[j].vecv_minus[1], Array_MVP[j].vecv_minus[2]),
            #     end="")

            # print("  %d  %8.4f"%(Array_MVP[j].type_mv, Array_MVP[j].vecdv_stw[1]) )

            # mvTimeStr ='[UTC]'+ "%04d"%(yr.value) +'-'+ "%02d"(mo.value) +'-'+ str(dy.value) +' '+ str(hr.value)+':'+str(mi.value)+':'+str(se.value)
            mvTimeStr = str("[UTC]%04d-%02d-%02d %02d:%02d:%07.4f"%(yr.value, mo.value, dy.value, hr.value, mi.value, se.value))
            mvObj = {
                'time':mvTimeStr,
                'vecr':[Array_MVP[j].vecr[0], Array_MVP[j].vecr[1], Array_MVP[j].vecr[2]],
                'vecv_minus':[Array_MVP[j].vecv_minus[0], Array_MVP[j].vecv_minus[1], Array_MVP[j].vecv_minus[2]],
                'vecdv_t':[Array_MVP[j].vecdv_stw[1]],
                'type':Array_MVP[j].type_mv
            }
            mvList.append(mvObj)
        # end of for

        reObj['moves'] = mvList
    # end of if
    return reObj

    #变轨后主目标星历计算

def getCalGTWObsError(
    N_LEAP, LEAPS,
    path_eph, path_gwt
):

    #----------------------------------------------------------------
    # 初始化cdll对象
    so_obj = cdll.LoadLibrary(c_lib_path)

    # 初始化全局参数+地球引力场参数(20X20)
    so_obj.dll_Initialization_Const()

    so_obj.dll_LoadGTWData.restype = c_int

    # 判断关键输入文件是否存在
    # path_eph = "./obs_error/eph_sp3_PC04.txt"
    # path_gwt = "./obs_error/20190206211725_107716_9602.GTW"
    if not os.path.exists(path_eph):
        print("Error: can not get .eph file!")
        return -1

    if not os.path.exists(path_gwt):
        print("Error: can not get .gwt file!")
        return -2

    # 常量
    NMAX_DATA = 86401 * 2
    NMAX_DATA_1 = NMAX_DATA + 1

    # 定义数据类型
    char600 = c_char * 600

    # fname_eop = char600()
    # fname_eop = create_string_buffer("./param/EOP-Last5Years.txt".encode(), 600)

    # 输入数据
    fname_gwt = char600()
    fname_gwt = create_string_buffer(path_gwt.encode(), 600)

    fname_eph = char600()
    fname_eph = create_string_buffer(path_eph.encode(), 600)

    # 读取GTW文件
    obsdata = (DObsData * NMAX_DATA_1)()
    nobs = c_int()
    satid = c_int()
    siteid = c_int()

    nobs = so_obj.dll_LoadGTWData(N_LEAP, LEAPS, fname_gwt, pointer(satid), pointer(siteid), obsdata)

    if nobs < NPOL_MAX:
        print("Warning: not enough data!")
        return -1

    flag = so_obj.dll_CalObsResidual_ObsData_GTW(N_LEAP, LEAPS, fname_eph, nobs, obsdata)

    if flag == 0:
        # 输出观测点残差
        for i in range(0, nobs):
            print("  %04d  %02d  %02d  %02d  %02d  %07.4f"%(
                obsdata[i].yr, obsdata[i].mo, obsdata[i].dy, obsdata[i].hr, obsdata[i].mi, obsdata[i].se), end="")
            
            tau = obsdata[i].tau
            dt = (obsdata[i].mjd - obsdata[0].mjd)*86400.0
            
            print("  %15.8f  %8.4f  %8.4f"%(obsdata[i].mjd, tau, dt), end="")

            # 赤经赤纬观测值ObsVal
            print("  %10.5f  %10.5f"%(
                obsdata[i].ObsVal[0] * Rad2Deg,
                obsdata[i].ObsVal[1] * Rad2Deg), end="")
            # 赤经赤纬残差值ResVal
            print("  %8.2f  %8.2f"%(
                obsdata[i].ResVal[0] * Rad2Deg*3600.0,
                obsdata[i].ResVal[1] * Rad2Deg*3600.0), end="")
            # 空间指向角度残差
            print("  %8.2f"%(obsdata[i].ResVal[2] * Rad2Deg*3600.0 ) )




