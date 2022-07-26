# -*- coding: utf-8 -*-
import datetime, time
import json

def output(msgType, msgObj):
    title = ''
    if 0 == msgType:
        title = 'Message '
    elif 1 == msgType:
        title = 'Progress'
    elif 2 == msgType:
        title = 'Warning '
    elif 3 == msgType:
        title = 'Error   '
    elif 4 == msgType:
        title = 'Result  '
    
    now = str(datetime.datetime.now())
    title = '['+title+' @ '+now+']'

    t = time.time()
    j = json.dumps(msgObj)
    print(title, j)

def message( msgObj, detailObj ):
    msgObj = { 'msg':msgObj, 'detail':detailObj }
    output(0, msgObj)

def progress( msgObj, detailObj ):
    msgObj = { 'msg':msgObj, 'detail':detailObj }
    output(1, msgObj)

def warning( msgObj, detailObj ):
    msgObj = { 'msg':msgObj, 'detail':detailObj }
    output(2, msgObj)

def error( msgObj, detailObj ):
    msgObj = { 'msg':msgObj, 'detail':detailObj }
    output(3, msgObj)

def result( msgObj, detailObj ):
    msgObj = { 'msg':msgObj, 'detail':detailObj }
    output(4, msgObj)


