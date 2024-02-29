package com.yhl.gj.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.dto.OrderDTO;
import com.yhl.gj.param.HistoryWarnResultRequest;
import com.yhl.gj.param.ResultQueryRequest;
import com.yhl.gj.param.TaskDirRequest;
import com.yhl.gj.param.WarnResultRequest;
import com.yhl.gj.service.ResultReceiveService;
import com.yhl.gj.service.WarnResultService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.Struct;

/**
 * 2022-11-08  新的变更单独放出来了
 */
@RestController
@RequestMapping("/api_v2")
public class ResultReceiveController {

    @Resource
    private ResultReceiveService resultReceiveService;
    /**
     * new   python 推送数据接口
     * @param request
     * @return
     */
    @PostMapping("/result")
    public Response resultPost(@RequestBody String  request) {
        JSONObject requestData = JSONObject.parseObject(request);
        JSONObject result = requestData.getJSONObject("result");

        return resultReceiveService.onReceiveData(result);
    }

    /**
     * 手动执行任务
     * @param orderRequest
     * @return
     */
    @PostMapping("/manualTask")
    public Response manualExecuteTask(@RequestBody @Valid OrderDTO orderRequest){
        return resultReceiveService.manualExecuteTask(orderRequest);
    }

    @GetMapping("/genOrderName")
    public Response genOrderName(){

        return Response.buildSucc(StrUtil.concat(true,"M",DateUtil.format(DateUtil.date(),"yyyyMMddHHmmssSSSSSS"))) ;
    }
    /**
     * 任务列表
     * @param request
     * @return
     */
    @PostMapping("/list")
    public Response resultList(@RequestBody @Valid ResultQueryRequest request){
        return resultReceiveService.queryResultByCondition(request);
    }

    /**
     * 任务详情
     * @param id
     * @return
     */
    @GetMapping("/getDetail")
    public Response getOne(@RequestParam("id") Long id){
        return resultReceiveService.getOne(id);
    }

    @GetMapping("/getMoves")
    public Response getMovesToFile(@RequestParam("id" )Long id,@RequestParam("type") String type){
        Assert.isTrue(StrUtil.equals("txt",type)||StrUtil.equals("csv",type),"type = [txt、csv]");
        return  resultReceiveService.getMovesToFiles(id,type);
    }    /**
     * 文件夹列表
     * @param request
     * @return
     * @throws IOException
     */
    @PostMapping("/taskDir" )
    public Response diskList(@RequestBody @Valid TaskDirRequest request) throws IOException {
        return  resultReceiveService.listDir(request.getDir());
    }

    /**
     * 卫星列表
     * @return
     */
    @GetMapping("/getSatellites")
    public Response getSatellites(){
        return resultReceiveService.getSatellites();
    }


    @GetMapping("/defaultParam")
    public Response getDefaultParam(){
        return resultReceiveService.getDefaultParam();
    }
    /**
     * 创建新任务
     * @return
     */
//    @PostMapping("/createNewTask")
//    public Response  createNewTask(){
//
//    }

    @PostMapping("/sendXmlToMq")
    public Response  sendXmlToMq(@RequestParam Long id){
       return  resultReceiveService.sendXmlToMq(id);
    }

    @Resource
    private WarnResultService warnResultService;
    @PostMapping("/warnResultQuery")
    public Response  warnResultQuery(@RequestBody @Valid WarnResultRequest request){
          return  warnResultService.warnResultQuery(request);
    }
    @PostMapping("/historyWarnResultQuery")
    public Response historyWarnResultQuery(@RequestBody @Valid HistoryWarnResultRequest request){
        return warnResultService.historyWarnResultQuery(request);
    }
    @PostMapping("/markedWarnResultToHistory/{warnId}")
    public Response markedWarnResultToHistory(@PathVariable("warnId") Long warnId){
        return warnResultService.markedWarnResultToHistory(warnId);
    }

    @PostMapping("markedAllWarnResultToHistory")
    public Response markedAllWarnResultToHistory(){
        return  warnResultService.markedAllWarnResultToHistory();
    }
}
