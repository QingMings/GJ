# -*- coding: utf-8 -*-
from ctypes import *
from math import sqrt, cos, sin, ceil
from random import random
import os

# so_obj = cdll.LoadLibrary("./lib/libDll_GEOAlarm.so")
so_obj = cdll.LoadLibrary("./lib/Dll_GEOAlarm.dll")

so_obj.dll_CalJTT.restype = c_double
so_obj.dll_CalJUT1.restype = c_double
so_obj.dll_CalAngle_SunPhase.restype = c_double
so_obj.dll_CalMeanValue.restype = c_double
so_obj.dll_GetTleEpoch.restype = c_double
so_obj.dll_InitializeTIMEArray.restype = c_int
so_obj.dll_JTT2JUTC.restype = c_double
so_obj.dll_LoadGTWData.restype = c_int
so_obj.dll_YMDHMS2JulianDate.restype = c_double
so_obj.GetCollisionAvoidanceStrategy.restype = c_int

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
        ("jtt_ca",          c_double),          # 接近距离极值时刻(JTT)
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

# 功能函数1-TLE根数轨道外推预报模块(SGP4模型)
def Call_OrbitProp_Tle():

    # 定义数据类型
    char600 = c_char * 600

    # 公共参数
    fname_lep = char600()
    fname_lep = create_string_buffer("./param/LEAP.txt".encode(), 600)

    fname_eop = char600()
    fname_eop = create_string_buffer("./param/EOP-Last5Years.txt".encode(), 600)

    # fpath_rst = char600()
    # fpath_rst = create_string_buffer("./output/".encode(), 600)

    # fname_out = char600()
    # fname_out = create_string_buffer("./output/temp_orbit_prop_result_eci_tle.txt".encode(), 600)
    fname_out = "./output/temp_orbit_prop_result_eci_tle.txt"

    N_LEAP = c_int()
    LEAPS  = (DLEAP * NMAX_LEAP)()

    N_EOP  = c_int()
    EOPS   = (DEOP * ( HalfLen_EOP * 2 + 1 ))()

    line1 = c_char * LTH_TLE
    line2 = c_char * LTH_TLE

    # flag = c_int
    # i = c_int

    yr = c_int()
    mo = c_int()
    dy = c_int()
    hr = c_int()
    mi = c_int()
    se = c_double()

    # jutcs = c_double()
    # jutce = c_double()
    # mjds  = c_double()
    # mjde  = c_double()
    # dt    = c_double()
    # step  = c_double()

    #NEPK     = c_int
    #Array_TT = POINTER(DTIME)
    #satephs  = POINTER(DSATEPH_CA)

    print("\ndll_Initialization_Const")
    so_obj.dll_Initialization_Const()

    print("\ndll_GetLEAPData")
    flag = so_obj.dll_GetLEAPData( fname_lep, pointer(N_LEAP), LEAPS)

    print("flag=", flag)
    print("N_LEAP=", N_LEAP)
    print("LEAPS=", LEAPS)

    line1 = create_string_buffer("1 45024U 20004A   21266.55389810  .00000115  00000-0  13145-4 0  9992".encode(), LTH_TLE)
    line2 = create_string_buffer("2 45024  86.4019 169.0662 0012085 280.5368  79.4493 14.75332375 90952".encode(), LTH_TLE)

    step = 5.0 / 1440.0 # 预报点输出步长[day]

    print("\ndll_YMDHMS2JulianDate")

    jutcs = so_obj.dll_YMDHMS2JulianDate(c_int(2021), c_int(9), c_int(24), c_int(0), c_int(0), c_double(0.0) )    #预报起始时刻[UTC]
    jutce = jutcs + 1.0

    print("jutcs=", jutcs)
    print("jutce=", jutce)

    print("\ndll_JulianDate2YMDHMS")
    so_obj.dll_JulianDate2YMDHMS( c_double( 0.5*(jutcs + jutce) ), pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se) )
    print( yr, mo, dy, hr, mi, se )

    print("\ndll_JulianDate2YMDHMS")
    flag = so_obj.dll_InitializeEOPArray( fname_eop, yr, mo, dy, 10, pointer(N_EOP), EOPS )
    print("flag=", flag)
    print("N_EOP=", N_EOP)
    print("EOPS=", EOPS)

    #定义预报时间
    mjds = jutcs - 2400000.5
    mjde = jutce - 2400000.5
    dt = mjde - mjds    #dt_simu应不超过10天

    pyNMAX_EPK = (int)(1440 * dt) + 100

    NMAX_EPK = c_int(pyNMAX_EPK)

    Array_TT = (DTIME * pyNMAX_EPK)()

    print("\ndll_InitializeTIMEArray")
    NEPK = so_obj.dll_InitializeTIMEArray(
        N_LEAP,
        LEAPS,
        N_EOP,
        EOPS,
        c_double(mjds),
        c_double(mjde),
        c_double(step),
        NMAX_EPK,
        pointer(Array_TT) )
    
    print("NEPK=", NEPK)
    print("Array_TT=", Array_TT)
    
    # for i in range(0, NEPK):
    #     print(Array_TT[i].JUTC)

    satephs = (DSATEPH_CA * NEPK)()

    print("\ndll_PropOrbit_Tle")
    flag = so_obj.dll_PropOrbit_Tle(
        N_LEAP,
        LEAPS,
        line1,
        line2,
        NEPK,
        pointer(Array_TT),
        pointer(satephs) )

    print("flag=", flag)

    if flag == 0:
        for i in range(0,NEPK):
            so_obj.dll_JulianDate2YMDHMS( c_double(Array_TT[i].JUTC), pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se) )
            print("  %04d  %02d  %02d  %02d  %02d  %09.6f"%(yr.value, mo.value, dy.value, hr.value, mi.value, se.value), end="" )
            print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%(
                satephs[i].vecr[0], satephs[i].vecr[1], satephs[i].vecr[2],
                satephs[i].vecv[0], satephs[i].vecv[1], satephs[i].vecv[2]) )

    return flag

# 功能函数2-KEPLER根数轨道外推预报模块(HPOP模型).
def Call_OrbitProp_KEPLER():
    # 定义数据类型
    char600 = c_char * 600

    # 公共参数
    fname_lep = char600()
    fname_lep = create_string_buffer("./param/LEAP.txt".encode(), 600)

    fname_eop = char600()
    fname_eop = create_string_buffer("./param/EOP-v1.1.txt".encode(), 600)

    fname_swd = char600()
    fname_swd = create_string_buffer("./param/SW-Last5Years.txt".encode(), 600)

    fname_out = "./output/temp_orbit_prop_result_eci_kpl.txt"

    N_LEAP = c_int()
    LEAPS  = (DLEAP * NMAX_LEAP)()

    N_EOP  = c_int()
    EOPS   = (DEOP * ( HalfLen_EOP * 2 + 1 ))()
    swdata = (DSWData * (HalfLen_Flux * 2 + 1) )()

    # 时间转换变量
    yr = c_int()
    mo = c_int()
    dy = c_int()
    hr = c_int()
    mi = c_int()
    se = c_double()

    # ele_in = (c_double * 6)()

    print("\ndll_Initialization_Const")
    so_obj.dll_Initialization_Const()

    print("\ndll_GetLEAPData")
    flag = so_obj.dll_GetLEAPData( fname_lep, pointer(N_LEAP), LEAPS)

    print("flag=", flag)
    print("N_LEAP=", N_LEAP)
    print("LEAPS=", LEAPS)

    step = 5.0 / 1440.0 # 预报点输出步长[day]

    print("\ndll_YMDHMS2JulianDate")
    jutcs = so_obj.dll_YMDHMS2JulianDate(c_int(2021), c_int(9), c_int(23), c_int(0), c_int(0), c_double(0.0) )    #预报起始时刻[UTC]
    jutce = jutcs + 1.0

    print("jutcs=", jutcs)
    print("jutce=", jutce)

    print("\ndll_JulianDate2YMDHMS")
    so_obj.dll_JulianDate2YMDHMS( c_double( 0.5*(jutcs + jutce) ), pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se) )
    print( yr, mo, dy, hr, mi, se )

    print("\ndll_JulianDate2YMDHMS")
    flag = so_obj.dll_InitializeEOPArray( fname_eop, yr, mo, dy, 10, pointer(N_EOP), EOPS )
    print("flag=", flag)
    print("N_EOP=", N_EOP)
    print("EOPS=", EOPS)

    # flag = dll_GetSpaceWeatherData(fname_swd, 0.5*(jutcs + jutce), 10, swdata, end="")
    print("\ndll_JulianDate2YMDHMS")
    flag = so_obj.dll_GetSpaceWeatherData( fname_swd, c_double( 0.5*(jutcs + jutce) ), 10, swdata )
    print("flag=", flag)
    print("swdata=", swdata)

    #定义预报时间
    mjds = jutcs - 2400000.5
    mjde = jutce - 2400000.5
    dt = mjde - mjds    #dt_simu应不超过10天

    pyNMAX_EPK = (int)(1440 * dt) + 100

    NMAX_EPK = c_int(pyNMAX_EPK)

    Array_TT = (DTIME * pyNMAX_EPK)()

    print("\ndll_InitializeTIMEArray")
    NEPK = so_obj.dll_InitializeTIMEArray(
        N_LEAP, LEAPS,
        N_EOP,  EOPS,
        c_double(mjds),
        c_double(mjde),
        c_double(step),
        NMAX_EPK,
        pointer(Array_TT) )
    
    print("NEPK=", NEPK)
    print("Array_TT=", Array_TT)

    # for i in range(0, NEPK):
    #     print(Array_TT[i].JUTC)    

	# 轨道外推预报
    mjd0 = so_obj.dll_YMDHMS2JulianDate(c_int(2021), c_int(9), c_int(23), c_int(0), c_int(0), c_double(0.0) ) - 2400000.50
    print("mjd0=", mjd0)

    # jtt0 = so_obj.dll_CalJTT( c_double(mjd0 + 2400000.5), N_LEAP, LEAPS )
    jtt0 = 2459480.5000000000000000
    print("jtt0=", jtt0)

    ele_in = (c_double * 6)(42164169.0, 0.00001, 0.02, 261.786, 0.0, 0.0)
    ele_kpl0 = (c_double * 6)()

    print("\ndll_UnitTransform_Orbit_Elements")
    so_obj.dll_UnitTransform_Orbit_Elements(0, ele_in, ele_kpl0)
    print("ele_kpl0=", ele_kpl0)
    for i in range(0,6):
        print("ele_kpl0[%d]=%f"%(i,ele_kpl0[i] ))

    # vecr = (c_double * 3)( -6023634.199, -41731252.346, 0.000 )
    # vecv = (c_double * 3)( 3043.152015, -439.259153, 1.07327 )

    AM0 = c_double(0.002)
    satephs = (DSATEPH_CA * NEPK)()

    flag = so_obj.dll_PropOrbit_Kepler(
        N_LEAP, LEAPS,
        N_EOP,  EOPS,
        swdata,
        NEPK,
        pointer(Array_TT),
        c_double(mjd0),
        c_double(jtt0),
        ele_kpl0,
        AM0,
        0,
        pointer(satephs) )
    
    print("flag=", flag)

    if flag == 0:
        for i in range(0,NEPK):
            so_obj.dll_JulianDate2YMDHMS( c_double(Array_TT[i].JUTC), pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se) )
            print("  %04d  %02d  %02d  %02d  %02d  %09.6f"%(yr.value, mo.value, dy.value, hr.value, mi.value, se.value), end="" )
            print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%(
                satephs[i].vecr[0], satephs[i].vecr[1], satephs[i].vecr[2],
                satephs[i].vecv[0], satephs[i].vecv[1], satephs[i].vecv[2]) )

    return flag

def FPrintf_CAResult_Single( satid_p, satid_s, ca_result ):
    # 主目标编号/从目标编号
    print("  %05d  %05d"%(satid_p, satid_s), end="")

    # 交会接近类型 1-极大值,0-极小值
    print("  %d"%(ca_result.type_ca), end="")

    # 交会接近时间 UTC
    yr = c_int()
    mo = c_int()
    dy = c_int()
    hr = c_int()
    mi = c_int()
    se = c_double()

    so_obj.dll_JulianDate2YMDHMS( c_double(ca_result.mjd_ca + 2400000.5), pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se) )
    print("  %04d  %02d  %02d  %02d  %02d  %09.6f"%(yr.value, mo.value, dy.value, hr.value, mi.value, se.value), end="")

    # 接近距离极值[m]
    print("  %10.1f"%(ca_result.dr_ca), end="")

    # UNW分量(从目标关于主目标UNW坐标系)
    vec_dXYZ = (c_double * 3)()
    so_obj.dll_ComparingOrbit_UNW(ca_result.vecr_p, ca_result.vecv_p, ca_result.vecr_s, vec_dXYZ)
    print("  %10.1f  %10.1f  %10.1f"%(vec_dXYZ[0], vec_dXYZ[1], vec_dXYZ[2]), end="")

    # 计算交会接近速度
    print("  %15.4f"%(ca_result.dv_ca), end="")

    # 交会角
    print("  %8.4f"%(ca_result.theta*Rad2Deg), end="")

    # tca时刻的主目标星下点位置LBH
    print("  %8.4f  %8.4f  %15.3f"%(ca_result.LBH[0] * Rad2Deg, ca_result.LBH[1] * Rad2Deg, ca_result.LBH[2] / 1000.0), end="")

    # 主目标位置速度
    print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%(
        ca_result.vecr_p[0], ca_result.vecr_p[1], ca_result.vecr_p[2],
        ca_result.vecv_p[0], ca_result.vecv_p[1], ca_result.vecv_p[2]), end="")

    # 从目标位置速度
    print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%(
        ca_result.vecr_s[0], ca_result.vecr_s[1], ca_result.vecr_s[2],
        ca_result.vecv_s[0], ca_result.vecv_s[1], ca_result.vecv_s[2]), end="")

    # 主目标预报期及STW位置误差
    print("  %15.6f"%(ca_result.dt_p), end="")
    print("  %15.3f  %15.3f  %15.3f"%(
        ca_result.sigma_p[0], ca_result.sigma_p[1], ca_result.sigma_p[2]), end="")

    # 从目标预报期及STW位置误差
    print("  %15.6f"%(ca_result.dt_s), end="")
    print("  %15.3f  %15.3f  %15.3f"%(
        ca_result.sigma_s[0], ca_result.sigma_s[1], ca_result.sigma_s[2]), end="")

    # 碰撞概率
    print("  %15.6e"%(ca_result.pc) )

def FPrintf_CATimeSpan_Single( ca_ts, n_ca_rst, ca_result):

    # 主目标编号/从目标编号
    print("  %05d  %05d"%(ca_ts.satid_p, ca_ts.satid_s), end="")

    # 进入接近距离门限范围内的时间
    yr = c_int()
    mo = c_int()
    dy = c_int()
    hr = c_int()
    mi = c_int()
    se = c_double()

    so_obj.dll_JulianDate2YMDHMS( c_double(ca_ts.mjds + 2400000.5), pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se) )
    print("  %04d  %02d  %02d  %02d  %02d  %09.6f"%(
        yr.value, mo.value, dy.value, hr.value, mi.value, se.value),
        end="")

    so_obj.dll_JulianDate2YMDHMS( c_double(ca_ts.mjde + 2400000.5), pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se) )
    print("  %04d  %02d  %02d  %02d  %02d  %09.6f"%(
        yr.value, mo.value, dy.value, hr.value, mi.value, se.value),
        end="")
    
    print("  %10.1f  %10.1f"%(ca_ts.drs, ca_ts.dre), end="")
    print("  %d  %d"%(ca_ts.n_ca_0, ca_ts.n_ca_1))

    for i in range(0, n_ca_rst):
        
        if (ca_result[i].mjd_ca < ca_ts.mjds):
            continue
		
        if (ca_result[i].mjd_ca > ca_ts.mjde):
            break

        FPrintf_CAResult_Single(ca_ts.satid_p, ca_ts.satid_s, ca_result[i])

# 功能函数3-交会接近事件计算
def Call_CloseApproach():

    # 定义数据类型
    char600 = c_char * 600

    # 公共参数
    fname_lep = char600()
    fname_lep = create_string_buffer("./param/LEAP.txt".encode(), 600)

    fname_eop = char600()
    fname_eop = create_string_buffer("./param/EOP-Last5Years.txt".encode(), 600)

    fname_swd = char600()
    fname_swd = create_string_buffer("./param/SW-Last5Years.txt".encode(), 600)

    # 初始化全局参数+地球引力场参数(20X20)
    # print("\ndll_Initialization_Const")
    so_obj.dll_Initialization_Const()

    # 读取跳秒值TAI-UTC
    N_LEAP = c_int()
    LEAPS  = (DLEAP * NMAX_LEAP)()

    # print("\ndll_GetLEAPData")
    flag = so_obj.dll_GetLEAPData( fname_lep, pointer(N_LEAP), LEAPS)
    # print("flag=", flag)
    # print("N_LEAP=", N_LEAP)
    # print("LEAPS=", LEAPS)

    # 场景初始化
    jtt_epk_p = c_double()
    jtt_epk_s = c_double()

    # ------------------------------------------------------------------------
	# 场景2
    dt = 7.0

    year = 2020
    month = 11
    day = 28
    hour = 12

    # 预报起止时间
    jutcs = so_obj.dll_YMDHMS2JulianDate(c_int(year), c_int(month), c_int(day), c_int(hour), c_int(0), c_double(0.0) )    #预报起始时刻[UTC]
    jutce = jutcs + dt
   
    # 预报步长
    lth_step = 300.0
    step = lth_step / 86400.0	#预报点输出步长[day]

    # 确定预报区间 中间点时间
    yr = c_int()
    mo = c_int()
    dy = c_int()
    hr = c_int()
    mi = c_int()
    se = c_double()

    # print("\ndll_JulianDate2YMDHMS")
    so_obj.dll_JulianDate2YMDHMS( c_double( 0.5*(jutcs + jutce) ), pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se) )
    # print( yr, mo, dy, hr, mi, se )

    #导入EOP参数
    N_EOP  = c_int()
    EOPS   = (DEOP * ( HalfLen_EOP * 2 + 1 ))()

    # print("\ndll_InitializeEOPArray")
    flag = so_obj.dll_InitializeEOPArray( fname_eop, yr, mo, dy, 10, pointer(N_EOP), EOPS )
    # print("flag=", flag)
    # print("N_EOP=", N_EOP)
    # print("EOPS=", EOPS)

    # 导入SPACEWEATHER参数
    swdata = (DSWData * (HalfLen_Flux * 2 + 1) )()

    # print("\ndll_GetSpaceWeatherData")
    flag = so_obj.dll_GetSpaceWeatherData( fname_swd, c_double( 0.5*(jutcs + jutce) ), 10, swdata )
    # print("flag=", flag)
    # print("swdata=", swdata)

    #定义预报时间
    mjds = jutcs - 2400000.5
    mjde = jutce - 2400000.5
    dt = mjde - mjds    #dt_simu应不超过10天
    print("mjds = %f, mjde = %f, dt = %f"%(mjds, mjde, dt))

    #初始化预报点时间数组
    pyNMAX_EPK = (int)(dt / step) + 100
    print("NMAX_EPK = %d, step = %f"%(pyNMAX_EPK, step) )

    NMAX_EPK = c_int(pyNMAX_EPK)
    Array_TT = (DTIME * pyNMAX_EPK)()

    # print("\ndll_InitializeTIMEArray")

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
    
    # print("NEPK=", NEPK)
    # print("Array_TT=", Array_TT)

    # ------------------------------------------------------------------------
    # 主目标参数
    # ------------------------------------------------------------------------

    # 主目标ID
    satid_p = 46610     #初始值

    # 主目标等效半径 [m]
    raddi_p = 5.0

    # 主目标面质比参数[m^2/kg]
    AM0_p = 0.005

    # 主目标轨道根数产生方式
    ele_shema_p = 3

    # 主目标外推轨道数据 初始化
    satephs_p = (DSATEPH_CA * NEPK)()

    # 两种不同数据输入格式
    if ele_shema_p == 2:    # TLE双行根数
        # TLE 双行根数数据
        char_tle_p1 = "1 46610U 20071A   20336.58729852 -.00000341 +00000-0 +00000-0 0  9996"
        char_tle_p2 = "2 46610 001.8858 281.7366 0002714 267.5035 210.9449 01.00272458000620"

        p_line1 = create_string_buffer(char_tle_p1.encode(), LTH_TLE)
        p_line2 = create_string_buffer(char_tle_p2.encode(), LTH_TLE)
        
        # 读取目标ID
        satid_p = int( char_tle_p1[2:7] )

        # 计算动力学时间
        jtt_epk_p = so_obj.dll_CalJTT(
            c_double(so_obj.dll_GetTleEpoch( p_line1 )), 
            N_LEAP,
            LEAPS )

        # TLE轨道外推预报
        flag = so_obj.dll_PropOrbit_Tle(
            N_LEAP,  LEAPS,
            p_line1, p_line2,
            NEPK,
            pointer(Array_TT),
            pointer(satephs_p) )
        
    elif ele_shema_p == 3:
        
        # 轨道根数类型
        type_ele_p = 0  # 0-历元+惯性系位置速度+面质比参数
                        # 1-历元+惯性系KEPLER根数+面质比参数

        # 计算动力学时间
        jutcs = so_obj.dll_YMDHMS2JulianDate(c_int(2020), c_int(11), c_int(28), c_int(12), c_int(0), c_double(0.0) )
        mjd0 = jutcs - 2400000.5
        jtt_epk_p = so_obj.dll_CalJTT( c_double(jutcs) ,N_LEAP, LEAPS )

        # 面质比参数[m^2/kg]
        # AM0_p = 0.005

        # 初始化KEPLER六根数[a/m,ecc/nan,incl-rad,RAAN-rad,w-rad,M-rad]
        ele_kpl0_p = (c_double * 6)()

        # 判断轨道根数类型
        if type_ele_p == 0:

            # 惯性系位置
            vr0 = (c_double * 3)( 41967380.822, 3963497.068, 1317690.371 )
            # 惯性系速度
            vv0 = (c_double * 3)( -289.337650, 3060.256783, 11.901635 )

            # 位置速度-->轨道根数(kepler)
            so_obj.dll_State_Transform_PosVel2ELE_kpl( vr0, vv0, ele_kpl0_p )

        elif type_ele_p == 1:
            # 惯性系位置速度
            data_ele = (c_double * 6)( 41967380.822, 3963497.068, 1317690.371, -289.337650, 3060.256783, 11.901635 )
        
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
            c_double(jtt_epk_p),
            ele_kpl0_p,
            c_double(AM0_p),
            0,
            pointer(satephs_p) )

    

    # ------------------------------------------------------------------------
    # 从目标参数
    # ------------------------------------------------------------------------

    # 从目标等效半径
    raddi_s = 5.0

    # 从目标面质比参数[m^2/kg]
    AM0_s = 0.005

    # TLE 双行根数
    char_tle_s1 = "1 46611U 20071A   20336.58729852 -.00000341 +00000-0 +00000-0 0  9996"
    char_tle_s2 = "2 46611 001.8858 281.7366 0008114 267.5035 210.9449 01.00293408000620"

    s_line1 = create_string_buffer(char_tle_s1.encode(), LTH_TLE)
    s_line2 = create_string_buffer(char_tle_s2.encode(), LTH_TLE)

    satid_s = int( char_tle_s1[2:7] )

    jtt_epk_s = so_obj.dll_CalJTT(
        c_double(so_obj.dll_GetTleEpoch( s_line1 )), 
        N_LEAP,
        LEAPS )

    satephs_s = (DSATEPH_CA * NEPK)()

    flag = so_obj.dll_PropOrbit_Tle(
        N_LEAP,  LEAPS,
        s_line1, s_line2,
        NEPK,
        pointer(Array_TT),
        pointer(satephs_s) )

	# ----------------------------------------------------------------------------------
    # 输出主目标/从目标轨道位置数据

    print("jtt_epk_p =", jtt_epk_p)
    print("jtt_epk_s =", jtt_epk_s)
    
    r_rel = (c_double*3)()
    v_rel = (c_double*3)()
    sun_angle = c_double()

    xn_avg = 0.0
    for i in range(0, NEPK):
        xn_avg += sqrt(MU / satephs_p[i].ele_kpl_avg[0] / satephs_p[i].ele_kpl_avg[0] / satephs_p[i].ele_kpl_avg[0])
    xn_avg = xn_avg / NEPK

    print("xn_avg = %8.6f"%(xn_avg) )

    for i in range(0, NEPK):
        so_obj.dll_CalRV_RefFrame(
            satephs_p[i].vecr, satephs_p[i].vecv,
            satephs_s[i].vecr, satephs_s[i].vecv,
            r_rel, v_rel )

        sun_angle = so_obj.dll_CalAngle_SunPhase(
            c_double(satephs_p[i].jtt),
            satephs_p[i].vecr,
            satephs_s[i].vecr )

        xc0 = 4.0*r_rel[0] + 2.0*v_rel[1] / xn_avg
        yc0 = r_rel[1] - 2.0*v_rel[0] / xn_avg

        a1 = (v_rel[0] / xn_avg)
        a2 = (3.0*r_rel[0] + 2.0*v_rel[1] / xn_avg)
        a = sqrt( a1 * a1 + a2 * a2)

        # print("i=%d, a=%f"%(i, a))

        dyc0 = -1.5*xc0*xn_avg*86400.0

        so_obj.dll_JulianDate2YMDHMS(
            c_double( Array_TT[i].JUTC ),
            pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se) )

        print("  %04d  %02d  %02d  %02d  %02d  %09.6f"%(
            yr.value, mo.value, dy.value, hr.value, mi.value, se.value),
            end="")
        print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%(
            satephs_p[i].vecr[0], satephs_p[i].vecr[1], satephs_p[i].vecr[2],
            satephs_p[i].vecv[0], satephs_p[i].vecv[1], satephs_p[i].vecv[2]),
            end="")
        print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%(
            satephs_s[i].vecr[0], satephs_s[i].vecr[1], satephs_s[i].vecr[2],
            satephs_s[i].vecv[0], satephs_s[i].vecv[1], satephs_s[i].vecv[2]),
            end="")
        print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%(
            r_rel[0], r_rel[1], r_rel[2], v_rel[0], v_rel[1], v_rel[2]),
            end="")
        print("  %15.3f"%(sqrt(r_rel[0] * r_rel[0] + r_rel[1] * r_rel[1] + r_rel[2] * r_rel[2])),
            end="")
        print("  %15.6f"%(sqrt(v_rel[0] * v_rel[0] + v_rel[1] * v_rel[1] + v_rel[2] * v_rel[2])),
            end="")
        print("  %8.4f"%(sun_angle * Rad2Deg) )

        print("  %15.3f  %15.3f  %15.3f  %15.3f"%(xc0, yc0, a, dyc0) )

    # -----------------------------------------------------------------------------
    # 交会接近筛查

    gate_dr = 100.0E+3
    gH = max(30000.0, 2.0*gate_dr)
    gD0 = max(150000.0, 5.0*gate_dr)			# [m],最近轨道距离筛选门限

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

    print("f_filter =", f_filter)

    # 无交会接近
    if f_filter < 0:
        return 0

    # 2.计算交会接近参数
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
        c_double(gate_dr),      # 接近距离门限[m]
                                # 用于计算相对距离小于该门限的时间区间, 并筛查最近接近距离小于该门限的最近接近事件
        # 输出
        pointer(n_ca_ts),       # 接近事件时间区间个数
        ca_ts,                  # 接近事件时间区间数组

        ca_rst                  # 交会接近分析输出结果数组
    )                           # n_ca_rst, 交会接近分析输出结果个数
    
    print("n_ca_rst =", n_ca_rst)
    print("n_ca_ts =", n_ca_ts)

    # 3.计算碰撞概率

    ellipsoid_type = 1          # 误差椭球坐标类型标识(1-STW, 2-UNW)

    for i in range(0, n_ca_rst):
        # 计算预报误差(这里先简化,实际需要读取预报误差参数文件,进行计算)
        ca_rst[i].sigma_p[0] = ca_rst[i].dt_p * 2.0E+3
        ca_rst[i].sigma_p[1] = ca_rst[i].dt_p * 5.0E+3
        ca_rst[i].sigma_p[2] = ca_rst[i].dt_p * 2.0E+3

        ca_rst[i].sigma_s[0] = ca_rst[i].dt_s * 2.0E+3
        ca_rst[i].sigma_s[1] = ca_rst[i].dt_s * 5.0E+3
        ca_rst[i].sigma_s[2] = ca_rst[i].dt_s * 2.0E+3

        # 不计算极大值时的碰撞概率
        if (ca_rst[i].type_ca == 1):
            continue

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

        print("i = %d, flag_pc = %d"%(i, flag_pc) )

        if ca_rst[i].pc < 1.0e-100:
            ca_rst[i].pc = 0.0

    # 4.输出主目标/从目标碰撞接近参数信息
    # for i in range (0, n_ca_ts.value):
    #     FPrintf_CATimeSpan_Single(ca_ts[i], n_ca_rst, ca_rst)

    # -----------------------------------------------------------------------------
    # 补充一个规避策略计算测试

    # ??系数
    coeff = 1.50

    # 危险接近事件筛查门限[m]
    # 当实际STW距离分量中的任一分量超出相应的门限值,认为不存在碰撞接近危险
    gate_dSTW = (c_double * 3)( 20.0E+3, 50.0E+3, 50.0E+3 )

    gate_dS = (c_double)( coeff * gate_dSTW[0] )
    gate_dT = (c_double)( coeff * gate_dSTW[1] )

    # 规避时间裕量门限[day], 对应于相邻两次变轨的最小时间间隔
    # 若当前待规避危险接近事件与上次规避危险接近事件的时间间隔小于该门限值, 认为规避策略失败
    gate_deltT = (c_double)( 6.0 / 24.0 )

    # 规避速度增量门限[m/s]
    # 当多个规避的速度增量总和超出该门限值,认为规避策略失败
    gate_deltV = (c_double)( 5.0 )

    # 内控参数
    lambda_ = (c_double)( 0.65 )    # 迭代改进控制系数 [无量纲]
    eps = (c_double)( 0.1 )         # 迭代改进收敛控制参数 [m]

    # 确定初始需要规避的目的接近事件
    # 方法: 遍历接近事件数组, 找到第一个极小值位置??
    # 反复赋值，作用是??
    pos_ca = 0
    for i in range (0, n_ca_rst ):
        if ca_rst[pos_ca].type_ca == 1 :
            pos_ca += 1
        else:
            break
    
    print("pos_ca=", pos_ca)

    pos_ca = n_ca_rst - 6
    pos_ca = 1

    # 首次变轨的参考时间, 首次变轨时间应发生在JTT_ref之后
    JTT_ref = c_double( ca_rst[pos_ca].jtt_ca - 1.0 )

    # 径向规避策略变轨时间控制;
    # 以目的接近事件时刻为参考, 提前NREV_S+0.5圈进行轨控
    NREV_S = c_int( 0 )

    # 迹向规避策略变轨时间控制;
    # 以目的接近事件时刻为参考, 提前NREV_T+1.0圈进行轨控
    NREV_T = c_int( 1 )

    # 从目标, 含有轨道外推从目标和雷达告警从目标，合并在一起
    nsat_s = c_int( 1 )                     # 从目标数量
    Array_satid_s = (c_int * 10)()          # 从目标ID 数组
    Array_JTT_epk_s = (c_double * 10)()     # 从目标轨道根数历元 数组
    Array_EPH_s = (DSATEPH_CA * (NEPK * nsat_s.value) )()   # 从目标的星历数据, 第i(i=0,...,nsat_s-1)个目标存放在[i*neph .. i*neph+neph-1]

    # 变轨事件记录
    Array_MVP = (DMVPara * 20 )()

    # 构建从目标数据
    Array_satid_s[0] = satid_s
    Array_JTT_epk_s[0] = jtt_epk_s

    for i in range( 0, nsat_s.value ):
        for j in range ( 0, NEPK ):
            Array_EPH_s[i*NEPK + j] = satephs_s[j]

    # 输出文件路径
    fname_mv = char600()
    fname_mv = create_string_buffer("temp_maneuver_shema.txt".encode(), 600)

    # 寻找规避策略
    num_mv = 0
    success = 0

    while( success== 0 ):
        num_mv = so_obj.GetCollisionAvoidanceStrategy(
            # 公共参数
            N_LEAP, LEAPS,
            N_EOP, EOPS,
            swdata,
            # 主目标
            satid_p,                    # 主目标(卫星)编号
            c_double(satephs_p[0].jtt), # 主目标(卫星)历元时刻
            satephs_p[0].ele_kpl_osc,   # 主目标(卫星)历元时刻的惯性系KEPLER根数[m/NaN/rad/rad/rad/rad]
            c_double(AM0_p),            # 主目标(卫星)面质比参数[m^2/kg]
            # 从目标
            nsat_s,                     # 从目标数量
            Array_satid_s,              # 从目标ID 数组
            Array_JTT_epk_s,            # 从目标轨道根数历元 数组
            
			NEPK,                       # 单个从目标星历数据点数
            Array_TT,                   # 从目标星历数据对应的历元时刻
            Array_EPH_s,                # 从目标的星历数据
            # 控制参数
            c_double(lth_step),         # 星历外推步长, 即相邻两组星历的历元时间间隔[sec]
			ca_rst[pos_ca],             # 初始需要规避的目的接近事件[DCA_Result]
			JTT_ref,                    # 首次变轨的参考时间
            NREV_S,                     # 径向规避策略变轨时间控制; 以目的接近事件时刻为参考, 提前NREV_S+0.5圈进行轨控
            NREV_T,                     # 迹向规避策略变轨时间控制; 以目的接近事件时刻为参考, 提前NREV_T+1.0圈进行轨控
            # 门限
			gate_dS,                    # 径向距离分量规避门限[m]
            gate_dT,                    # 迹向距离分量规避门限[m]
            c_double(gate_dr),          # 接近事件筛查距离门限[m] 
            gate_dSTW,                  # 危险接近事件筛查门限[m]. 当实际STW距离分量中的任一分量超出相应的门限值,认为不存在碰撞接近危险
			gate_deltT,                 # 规避时间裕量门限[day]. 对应于相邻两次变轨的最小时间间隔; 若当前待规避危险接近事件与上次规避危险接近事件的时间间隔小于该门限值,认为规避策略失败
            gate_deltV,                 # 规避速度增量门限[m/s]. 当多个规避的速度增量总和超出该门限值, 认为规避策略失败
			# 内控参数
            lambda_,                    # 迭代改进控制系数[无量纲]
            eps,                        # 迭代改进收敛控制参数[m]
            c_double(0.25),             # 无量纲系数,取值范围[0..1],优选规避策略用
            c_int(1),                   # 输出控制,1-输出调试信息
			fname_mv,                   # 规避方案输出文件名
			Array_MVP                   # 输出: 变轨事件记录
        )                               # num_mv为机动次数(函数返回值)
                                        # >0 : 正常退出,机动规避次数
                                        # -1 : 规避策略制定失败
                                        # -2 : 首次规避时间裕量不足
        print("num_mv = ", num_mv)
        if num_mv <= 0 :
            coeff += 0.5
            gate_dS = coeff*gate_dSTW[0]
            gate_dT = coeff*gate_dSTW[1]

            if coeff > 2.2 :
                print("未能寻找到合适的规避策略!\n")
                break
        else:
            success = 1
            # print("\n\n  规避策略: 变轨次数 = %2d, 总速度变量 = %8.4f\n"%( num_mv, Array_MVP[num_mv - 1].DeltV_all) )
            for j in range (0, num_mv):
                print("j=", j)

                so_obj.dll_JulianDate2YMDHMS(
                    c_double(so_obj.dll_JTT2JUTC(
                        Array_MVP[j].jtt,
                        N_LEAP, LEAPS
                    )),
                    pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se)
                )

                print("    第%2d次变轨:  %04d-%02d-%02d %02d:%02d:%07.4f"%(
                    j+1, yr.value, mo.value, dy.value, hr.value, mi.value, se.value),
                    end="")
                
                print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%(
                    Array_MVP[j].vecr[0], Array_MVP[j].vecr[1], Array_MVP[j].vecr[2],
                    Array_MVP[j].vecv_minus[0], Array_MVP[j].vecv_minus[1], Array_MVP[j].vecv_minus[2]),
                    end="")

                print("  %d  %8.4f"%(Array_MVP[j].type_mv, Array_MVP[j].vecdv_stw[1]) )

    #变轨后主目标星历计算

# 读simu_obs 文件
def LoadAERData(N_LEAP, LEAPS, 
    fin_aer,
    n_skip, n_max,
    array_utc, array_tt,
    array_azi, array_elv, array_rho,
    array_r_tgt
    ):
    # 打开文件
    fh = open(fin_aer, 'r')
    # 打开文件成功
    if fh:
        # 读全部行
        lines = fh.readlines()
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
        # 关闭文件handler
        fh.close()
        # 返回值
        return n_data

    # 打开文件成功失败
    else:
        print("warnning: can not open file %s.\n", fin_aer)
        return -1

def dll_OrbitProp_ECIRV(
    N_LEAP,LEAPS,
	N_EOP, EOPS,
	fname_swd,
    jtt0, vr0, vv0,
    AM0, NEPK,
    Array_TT, 
    ptr_sateph
):
    swdata = (DSWData * (HalfLen_Flux * 2 + 1) )()

    # 初始化时间参数
    yr = c_int()
    mo = c_int()
    dy = c_int()
    hr = c_int()
    mi = c_int()
    se = c_double()

    # 初始化KEPLER六根数[a/m,ecc/nan,incl-rad,RAAN-rad,w-rad,M-rad]
    ele_kpl0_p = (c_double * 6)()

    # 时间中点
    t_mid = c_double(0.5*(Array_TT[0].JUTC + Array_TT[NEPK - 1].JUTC) )
    print("t_mid = ", t_mid )

    so_obj.dll_JulianDate2YMDHMS( t_mid, pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se) )

    flag = so_obj.dll_GetSpaceWeatherData( fname_swd, t_mid, 10, swdata )

    mjd0 = so_obj.dll_JTT2JUTC( c_double(jtt0), N_LEAP, LEAPS ) - 2400000.5
    print("mjd0 = ", mjd0 )
    print("jtt0 = ", jtt0 )

	# 位置速度-->轨道根数(kepler)
    so_obj.dll_State_Transform_PosVel2ELE_kpl( vr0, vv0, ele_kpl0_p )

    # print(ele_kpl0_p[0], ele_kpl0_p[1], ele_kpl0_p[2], ele_kpl0_p[3], ele_kpl0_p[4], ele_kpl0_p[5])

	# 利用主目标(卫星)KEPLER根数进行轨道外推

    flag = so_obj.dll_PropOrbit_Kepler(
        N_LEAP, LEAPS,
        N_EOP,  EOPS,
        swdata,
        NEPK,
        pointer(Array_TT),
        c_double(mjd0),
        c_double(jtt0),
        ele_kpl0_p,
        c_double(AM0),
        0,
        ptr_sateph
    )

    return flag

def Call_RadarAlarm():
    # 常量
    NMAX_DATA = 86401 * 2

    # 定义数据类型
    char600 = c_char * 600

    # 公共参数
    fname_lep = char600()
    fname_lep = create_string_buffer("./param/LEAP.txt".encode(), 600)

    fname_eop = char600()
    fname_eop = create_string_buffer("./param/EOP-Last5Years.txt".encode(), 600)

    fname_swd = char600()
    fname_swd = create_string_buffer("./param/SW-Last5Years.txt".encode(), 600)

    # 初始化全局参数+地球引力场参数(20X20)
    so_obj.dll_Initialization_Const()

    # 读取跳秒值TAI-UTC
    N_LEAP = c_int()
    LEAPS  = (DLEAP * NMAX_LEAP)()
    flag = so_obj.dll_GetLEAPData( fname_lep, pointer(N_LEAP), LEAPS)

    # 测量误差先验信息+改进收敛门限(做成外部配置参数)
    sig_rho = c_double(10.0)                # [m]
    sig_azi = c_double(0.05*Deg2Rad)
    sig_elv = c_double(0.05*Deg2Rad)
    GATE_CONVG_RES_RMS = c_double( 500.0 )  # [m]

    array_utc = (c_double * NMAX_DATA)()
    array_tt = (c_double * NMAX_DATA)()
    array_ut1 = (c_double * NMAX_DATA)()
    array_azi = (c_double * NMAX_DATA)()
    array_elv = (c_double * NMAX_DATA)()
    array_rho = (c_double * NMAX_DATA)()
    NMAX_DATAx3 = NMAX_DATA * 3
    array_r_tgt = (c_double * NMAX_DATAx3)()

    # 读取输入文件
    # fname_aer = char600()
    # fname_aer = create_string_buffer("./input/simu_obs_aer_self.txt".encode(), 600)
    fname_aer = "./input/simu_obs_aer_self.txt"
    # 跳过数据
    nskip = 0 * 60
    # 调试参数,控制数据使用长度
    nmax = 181
    ndata = LoadAERData(N_LEAP, LEAPS, fname_aer, 
        nskip, nmax,
        array_utc, array_tt,
		array_azi, array_elv, array_rho, 
        array_r_tgt)

    # for i in range( 0, ndata ):
    #     print("\n i = %d, jutc = %f, jtt = %f"%(i, array_utc[i], array_tt[i]))
        # print("\n i = %d, rx = %f, ry = %f, rz = %f"%(i, array_r_tgt[i * 3], array_r_tgt[i*3+1], array_r_tgt[i*3+2]))

    # 利用观测数据UTC时间, 计算UT1时间
    yr = c_int()
    mo = c_int()
    dy = c_int()
    hr = c_int()
    mi = c_int()
    se = c_double()

    so_obj.dll_JulianDate2YMDHMS( c_double( 0.5*(array_tt[0] + array_tt[ndata - 1]) ), 
        pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se) )

    N_EOP  = c_int()
    EOPS   = (DEOP * ( HalfLen_EOP * 2 + 1 ))()
    flag = so_obj.dll_InitializeEOPArray( fname_eop, yr, mo, dy, 10, pointer(N_EOP), EOPS )

    for i in range( 0, ndata ):
        array_ut1[i] = so_obj.dll_CalJUT1(c_double(array_utc[i]), N_EOP, EOPS)
        # print( "i=%d, jut1=%14.6f"%(i,array_ut1[i]) )

    # 计算实测x/y/z值
    ndatax3 = ndata * 3
    array_r_rel_obs = (c_double * ndatax3)()
    for i in range( 0, ndata ):
        rx = array_rho[i] * cos(array_elv[i])*cos(array_azi[i])     # In-Track
        ry = -array_rho[i] * cos(array_elv[i])*sin(array_azi[i])    # Cross-Track
        rz = array_rho[i] * sin(array_elv[i])                       # Radial
        # 注意与CalAER()函数传参顺序
        array_r_rel_obs[i * 3 + 0] = rz
        array_r_rel_obs[i * 3 + 1] = rx
        array_r_rel_obs[i * 3 + 2] = ry

    # 计算初始状态值X0_ini
    # 初始状态值定义为六维矢量从目标相对于主目标的位置/速度[x0,y0,z0,dx0,dy0,dz0]
    npol = NPOL_MAX
    X0_ini = (c_double * 6)()
    # print("0: array_tt[0] =", array_tt[0])
    jutc = so_obj.dll_JTT2JUTC( c_double(array_tt[0]), N_LEAP, LEAPS )
    # print("1: jutc =", jutc)
    so_obj.dll_JulianDate2YMDHMS( c_double( jutc ), 
        pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se) )
    jutc = so_obj.dll_YMDHMS2JulianDate(yr, mo, dy, hr, mi, c_double(0.0) )
    # print("2: jutc =", jutc)
    tt0 = so_obj.dll_CalJTT( c_double(jutc), N_LEAP, LEAPS )
    # print("3: tt0 =", tt0)
    flag = so_obj.GetInitialStateValue(
        ndata,
        array_tt,
        array_r_rel_obs,
        npol,
        c_double(tt0),
        X0_ini
    )

    # for i in range(0, 6):
    #     print( "X0_ini[%d] = %f\t"%(i, X0_ini[i]), end="")
    # print()

    # 利用观测数据中给出的卫星位置数据,计算各观测点卫星(目标星)位置、速度值.
	# 同时输出卫星平均运动角速度数据array_xn[]
    ndata_1 = ndata + 1
    ndata_1x3 = ndata_1 * 3

    sateph = (DSATEPH_CA * ndata_1)()
    array_xn = (c_double * ndata_1)()
    #npol为内插多项式的阶数
    dt_max = npol / 1440.0  # 内插用数据范围的控制参数[day]
    neph = so_obj.dll_GetSatEphBasedOnXYZ(
        ndata, array_utc, array_ut1, array_tt, array_r_tgt, c_double(dt_max), npol,     # input
        sateph, array_xn                                                                # output
    )

    print("neph =", neph)
    print("ndata =", ndata)

    if (neph != ndata):
        print("warnning: 卫星(目标星)位置内插错误,退出.\n")
        return(-3)
    
    # 对初始状态值X0_ini进行迭代修正
    array_dt = (c_double * ndata_1)()
    array_azi_c = (c_double * ndata_1)()
    array_elv_c = (c_double * ndata_1)()
    array_rho_c = (c_double * ndata_1)()
    res = (c_double * ndata_1x3)()

    nused = ndata

    for i in range( 0, ndata ):
        array_dt[i] = (array_tt[i] - tt0)*86400.0   #[sec]
    
    xn_avg = so_obj.dll_CalMeanValue(nused, array_xn)

    X0_new = (c_double * 6)()
    var = (c_double * 36)()

    flag_rst = so_obj.dll_RelativeMotionCorrection(
        nused, array_dt, array_xn,
		array_azi, array_elv, array_rho,                    # in
		array_azi_c, array_elv_c, array_rho_c,              # out
		sig_rho, sig_azi, sig_elv, GATE_CONVG_RES_RMS,      # in
		c_double(tt0), X0_ini,                                        # in
        X0_new, res, var                                    # out
    )

    # for i in range( 0, ndata_1 ):
    #     print( "%d, array_azi_c[]=%f, array_elv_c[]=%f, array_rho_c[]=%f "%( i,array_azi_c[i],  array_elv_c[i], array_rho_c[i] ) )

    # 初值迭代修正失败
    if (flag_rst > 2):
        print( "转直接用array_r_rel_obs[]趋势进行判断") 

    # 观测段结果
    neph_1 = neph + 1
    X_rel = ( DSATEPH_CA * neph_1)()

    # 采用简化一阶C-W模型,外推相对运动状态.
    so_obj.dll_PropRelMotionState_AvgXn(
        neph, array_dt,     # input
        c_double(xn_avg),
        c_double(tt0),
        X0_new,             
        X_rel               # output
    )

    # 输出
    ctrl_output = 0
    if ctrl_output == 1 :
        rIs = (c_double*3)()
        vIs = (c_double*3)()
        for i in range (0, neph):

            so_obj.dll_JulianDate2YMDHMS(
                c_double( array_utc[i] ),
                pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se)
            )

            # 计算从目标位置速度
            so_obj.dll_RecoverRV_ECIFrame(
                sateph[i].vecr, sateph[i].vecv, X_rel[i].vecr, X_rel[i].vecv,
                rIs, vIs
            )

            print("  %04d  %02d  %02d  %02d  %02d  %09.6f"%(
                yr.value, mo.value, dy.value, hr.value, mi.value, se.value),
                end="")

            print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%(
                sateph[i].vecr[0], sateph[i].vecr[1], sateph[i].vecr[2],
                sateph[i].vecv[0], sateph[i].vecv[1], sateph[i].vecv[2]),
                end="")
            
            print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%(
                rIs[0], rIs[1], rIs[2],
                vIs[0], vIs[1], vIs[2],),
                end="")

            print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%(
                X_rel[i].vecr[0], X_rel[i].vecr[1], X_rel[i].vecr[2],
                X_rel[i].vecv[0], X_rel[i].vecv[1], X_rel[i].vecv[2]),
                end="")

            if (i < nused):
                print("  %15.3f  %8.4f  %8.4f  %15.3f  %8.4f  %8.4f  %15.3f  %8.4f  %8.4f"%(
					array_rho[i], array_azi[i] * Rad2Deg, array_elv[i] * Rad2Deg,
					array_rho_c[i], array_azi_c[i] * Rad2Deg, array_elv_c[i] * Rad2Deg,
					res[i * 3 + 0], res[i * 3 + 1], res[i * 3 + 2]),
                    end="")
            
            print()

    # 利用修正后的初始相对运动状态,外推预报未来一段时间范围内的相对运动参数(相对位置，相对速度)
    # 定义预报时间
    step = 300.0                                # [sec],输出时间步长.
    nstep = (ceil(86400.0 / step)) * 7 + 1      # 外推N(N=7)天
    nstep_1= nstep + 1
    NMAX_EPK = nstep + 100
    # utc0 = so_obj.dll_JTT2JUTC( c_double(tt0), N_LEAP, LEAPS)
    mjds = so_obj.dll_JTT2JUTC( c_double(tt0), N_LEAP, LEAPS) - 2400000.5                           # 起始时刻
    mjde = so_obj.dll_JTT2JUTC( c_double(tt0 + nstep*step / 86400.0) , N_LEAP, LEAPS) - 2400000.5   # 结束时刻

    # 构造时间结构体
    Array_TT = (DTIME * NMAX_EPK)()
    NEPK = so_obj.dll_InitializeTIMEArray(
        N_LEAP,
        LEAPS,
        N_EOP,
        EOPS,
        c_double(mjds),
        c_double(mjde),
        c_double(step / 86400.0 ),
        NMAX_EPK,
        pointer(Array_TT)
    )
    print("NEPK = ", NEPK  )
    AM0 = 0.005

    array_dt = (c_double * nstep_1)()
    sateph_p = (DSATEPH_CA * nstep_1)()
    sateph_s = (DSATEPH_CA * nstep_1)()
    X_rel = (DSATEPH_CA * nstep_1)()

    # 主目标(卫星)轨道外推
    # 备注: 此处需要使用外部输入的卫星轨道根数生成(sateph[0].jtt, sateph[0].vecr, sateph[0].vecv, AM0)参数!!!
    # print("sateph[0].jtt=", sateph[0].jtt )

    flag = dll_OrbitProp_ECIRV(
        N_LEAP, LEAPS,
        N_EOP, EOPS,
        fname_swd,
		sateph[0].jtt,
        sateph[0].vecr,
        sateph[0].vecv,
        AM0, NEPK,
        Array_TT,
        pointer(sateph_p)
    )

    # for i in range(0, nstep_1):
    #     print("i=%d, sateph_p[i].jutc = %f"% ( i, sateph_p[i].jutc) )

    # 从目标相对于主目标的位置/速度外推
    for i in range(0, nstep):
        array_dt[i] = i*step
    
    so_obj.dll_PropRelMotionState_AvgXn(nstep, array_dt, c_double(xn_avg), c_double(tt0), X0_new, X_rel)

    # 从目标J2000惯性系位置速度计算
    for i in range(0, nstep):
        sateph_s[i].jutc = sateph_p[i].jutc
        sateph_s[i].jtt = sateph_p[i].jtt
        sateph_s[i].jut1 = sateph_p[i].jut1
        # 利用主目标(卫星)J2000惯性系位置速度,以及从目标相对于主目标的位置速度,计算从目标J2000惯性系位置速度
        so_obj.dll_RecoverRV_ECIFrame(
            sateph_p[i].vecr, sateph_p[i].vecv, X_rel[i].vecr, X_rel[i].vecv,
            sateph_s[i].vecr, sateph_s[i].vecv
        )
        # 从目标位置速度-->KEPLER瞬根
        so_obj.dll_State_Transform_PosVel2ELE_kpl(
            sateph_s[i].vecr, sateph_s[i].vecv,     # in
            sateph_s[i].ele_kpl_osc                 # out
        )
        # 从目标KEPLER瞬根-->KEPLER平根
        so_obj.dll_Osculating2AverageEle(
            c_double(sateph_s[i].jut1),
            c_double(sateph_s[i].jtt),
            sateph_s[i].ele_kpl_osc,    # in
            sateph_s[i].ele_kpl_avg     # out
        )

    # 输出
    ctrl_output = 1
    if ctrl_output == 1 :
        for i in range(0, nstep):
            sun_angle = so_obj.dll_CalAngle_SunPhase(
                c_double(sateph_p[i].jtt),
                sateph_p[i].vecr,
                sateph_s[i].vecr
            )

            xc0 = 4.0*X_rel[i].vecr[0] + 2.0*X_rel[i].vecr[1] / xn_avg
            yc0 = X_rel[i].vecr[1] - 2.0*X_rel[i].vecr[0] / xn_avg

            a1 = (X_rel[i].vecv[0] / xn_avg)
            a2 = (3.0*X_rel[i].vecr[0] + 2.0*X_rel[i].vecv[1] / xn_avg)
            a = sqrt( a1 * a1 + a2 * a2)

            dyc0 = -1.5*xc0*xn_avg*86400.0

            jutc = so_obj.dll_JTT2JUTC( c_double( tt0 + array_dt[i] / 86400.0 ), N_LEAP, LEAPS)
            print("i = %d, jutc=%f\n"%( i, jutc) )

            so_obj.dll_JulianDate2YMDHMS(
                c_double( jutc ),
                pointer(yr), pointer(mo), pointer(dy), pointer(hr), pointer(mi), pointer(se) )

            print("  %04d  %02d  %02d  %02d  %02d  %09.6f"%(
                yr.value, mo.value, dy.value, hr.value, mi.value, se.value),
                end="")

            print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%(
                sateph_p[i].vecr[0], sateph_p[i].vecr[1], sateph_p[i].vecr[2],
                sateph_p[i].vecv[0], sateph_p[i].vecv[1], sateph_p[i].vecv[2]),
                end="")
            print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%(
                sateph_s[i].vecr[0], sateph_s[i].vecr[1], sateph_s[i].vecr[2],
                sateph_s[i].vecv[0], sateph_s[i].vecv[1], sateph_s[i].vecv[2]),
                end="")
            
            r0 = X_rel[i].vecr[0]
            r1 = X_rel[i].vecr[1]
            r2 = X_rel[i].vecr[2]
            v0 = X_rel[i].vecv[0]
            v1 = X_rel[i].vecv[1]
            v2 = X_rel[i].vecv[2]

            print("  %15.3f  %15.3f  %15.3f  %15.6f  %15.6f  %15.6f"%( r0, r1, r2, v0, v1, v2), end="")
            print("  %15.3f"%(sqrt( r0*r0 + r1*r1 + r2*r2 )), end="")
            print("  %15.6f"%(sqrt( v0*v0 + v1*v1 + v2*v2 )), end="")

            print("  %8.4f"%(sun_angle * Rad2Deg) )

            print("  %15.3f  %15.3f  %15.3f  %15.3f"%(xc0, yc0, a, dyc0) )
        # end of for
    # end of if
# end of def

def Call_CalGTWObsError():
    # 判断关键输入文件是否存在
    fname_eph_py = "./obs_error/eph_sp3_PC04.txt"
    if not os.path.exists(fname_eph_py):
        print("Error: can not get .eph file!")
        return -2

    # 常量
    NMAX_DATA = 86401 * 2
    NMAX_DATA_1 = NMAX_DATA + 1

    # 定义数据类型
    char600 = c_char * 600

    # 公共参数
    fname_lep = char600()
    fname_lep = create_string_buffer("./param/LEAP.txt".encode(), 600)

    # fname_eop = char600()
    # fname_eop = create_string_buffer("./param/EOP-Last5Years.txt".encode(), 600)

    # 输入数据
    fname_obs = char600()
    fname_obs = create_string_buffer("./obs_error/20190206211725_107716_9602.GTW".encode(), 600)

    fname_eph = char600()
    fname_eph = create_string_buffer(fname_eph_py.encode(), 600)

    # 初始化全局参数+地球引力场参数(20X20)
    so_obj.dll_Initialization_Const()

    # 读取跳秒值TAI-UTC
    N_LEAP = c_int()
    LEAPS  = (DLEAP * NMAX_LEAP)()
    flag = so_obj.dll_GetLEAPData( fname_lep, pointer(N_LEAP), LEAPS)

    # 读取GTW文件
    obsdata = (DObsData * NMAX_DATA_1)()
    nobs = c_int()
    satid = c_int()
    siteid = c_int()

    nobs = so_obj.dll_LoadGTWData(N_LEAP, LEAPS, fname_obs, pointer(satid), pointer(siteid), obsdata)

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

if __name__ == "__main__":

    # flag = Call_OrbitProp_Tle()

    # flag = Call_OrbitProp_KEPLER()

    # Call_CloseApproach()

    # Call_RadarAlarm()

    Call_CalGTWObsError()
