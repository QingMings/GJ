# -*- coding: utf-8 -*-
from typing import OrderedDict
import xml.dom.minidom

def parse( filepath ):

    dom = xml.dom.minidom.parse( filepath )

    # process-order
    root = dom.documentElement  
    
    # process-order -> task
    node_task = root.getElementsByTagName("task")[0]

    # task_priority   = node_task.getAttribute("priority")
    task_order_id   = node_task.getAttribute("orderid")
    # task_name       = node_task.getAttribute("name")
    # task_id         = node_task.getAttribute("id")
    task_type         = node_task.getAttribute("task_type")

    # process-order -> task -> input
    node_input = node_task.getElementsByTagName("inputfilelist")[0]
    input_num = node_input.getAttribute("num")

    # process-order -> task -> input -> PATH_SATELLITE
    nodes_path_satellite = node_input.getElementsByTagName("PATH_SATELLITE")
    path_satellite = ''
    if nodes_path_satellite:
        path_satellite = nodes_path_satellite[0].childNodes[0].data

    # process-order -> task -> input -> PATH_TARGET_ORBIT
    nodes_orbit_target = node_input.getElementsByTagName("PATH_TARGET_ORBIT_LIST")

    if nodes_orbit_target:
        orbit_target = nodes_orbit_target[0]
        orbit_target_num = orbit_target.getAttribute("num")
        num = int(orbit_target_num)
        orbit_target_list = []
        for i in range(0, num):
            orbit_target_list.append( orbit_target.getElementsByTagName("TARGET_ORBIT")[i].childNodes[0].data  )

    # process-order -> task -> input -> PATH_TARGET_RADAR_LIST
    radar_target = node_input.getElementsByTagName("PATH_TARGET_RADAR_LIST")[0]
    radar_target_num = radar_target.getAttribute("num")

    num = int(radar_target_num)
    radar_target_list = []
    for i in range(0, num):
        radar_target_list.append( radar_target.getElementsByTagName("TARGET_RADAR")[i].childNodes[0].data  )

    # process-order -> task -> input -> PATH_TARGET_LASER
    path_target_laser = node_input.getElementsByTagName("PATH_TARGET_LASER")[0].childNodes[0].data

    # process-order -> task -> input -> PATH_OBS_GTW
    path_obs_gtw = node_input.getElementsByTagName("PATH_OBS_GTW")[0].childNodes[0].data

    # process-order -> task -> input -> PATH_OBS_EPH
    path_obs_eph = node_input.getElementsByTagName("PATH_OBS_EPH")[0].childNodes[0].data

    # process-order -> task -> output
    node_output = node_task.getElementsByTagName("outputfilelist")[0]
    path_output = node_output.getElementsByTagName("OUTPUT")[0].childNodes[0].data

    # process-order -> task -> params
    node_params = node_task.getElementsByTagName("params")[0]

    # process-order -> task -> params -> GATES
    path_gates = node_params.getElementsByTagName("GATES")[0].childNodes[0].data

    # process-order -> task -> params -> TIME_SPAN
    node_time_span = node_params.getElementsByTagName("TIME_SPAN")[0]
    time_start_utc = node_time_span.getAttribute("start_time_utc")
    time_unit = node_time_span.getAttribute("unit")
    time_span = node_time_span.childNodes[0].data

    # process-order -> task -> params -> PARMA_LEAP
    path_leap = node_params.getElementsByTagName("PARMA_LEAP")[0].childNodes[0].data

    # process-order -> task -> params -> PARMA_EOP
    path_eop = node_params.getElementsByTagName("PARMA_EOP")[0].childNodes[0].data

    # process-order -> task -> params -> PARMA_SWD
    path_swd = node_params.getElementsByTagName("PARMA_SWD")[0].childNodes[0].data

    # process-order -> task -> params -> PARMA_ERR
    path_error = node_params.getElementsByTagName("PARMA_ERR")[0].childNodes[0].data

    # 构造订单信息dict
    orderinfo= {
        'task':{
            # 'priority':task_priority,
            'type':task_type,
            'order_id':task_order_id
            # 'name':task_name,
            # 'id':task_id
        },
        'input':{
            'num':input_num,
            'path_satellite':path_satellite,
            'num_target_orbit':orbit_target_num,
            'path_target_orbit_list':orbit_target_list,
            'num_target_orbit':radar_target_num,
            'path_target_radar_list':radar_target_list,
            'path_target_laser':path_target_laser,
            'path_obs_gtw':path_obs_gtw,
            'path_obs_eph':path_obs_eph
        },
        'output':path_output,
        'params':{
            'time_start_utc':time_start_utc,
            'time_unit':time_unit,
            'time_span':time_span,
            'path_gates':path_gates,
            'path_leap':path_leap,
            'path_eop':path_eop,
            'path_swd':path_swd,
            'path_error':path_error,
        }
    }

    return orderinfo

    
