package com.tugos.dst.admin.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.google.common.collect.Lists;
import com.tugos.dst.admin.enums.DstLogTypeEnum;
import com.tugos.dst.admin.utils.DstConfigData;
import com.tugos.dst.admin.utils.DstConstant;
import com.tugos.dst.admin.utils.FileUtils;
import com.tugos.dst.admin.vo.ScheduleVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author qinming
 * @date 2020-10-25 21:46:53
 * <p> 系统服务 </p>
 */
@Service
public class SystemService {

    /**
     * 拉取dst游戏日志
     * @param type 0地面日志 ，1 洞穴日志 2 玩家聊天记录
     * @param rowNum 日志的行数，从后开始取
     * @return 日志
     */
    public List<String> getDstLog(Integer type, Integer rowNum) {
        String path;
        switch (Objects.requireNonNull(DstLogTypeEnum.get(type))){
            case CAVES_LOG:
                //洞穴
                path = DstConstant.ROOT_PATH + DstConstant.SINGLE_SLASH + DstConstant.DST_CAVES_SERVER_LOG_PATH;
                break;
            case CHAT_LOG:
                //聊天
                path = DstConstant.ROOT_PATH + DstConstant.SINGLE_SLASH + DstConstant.DST_MASTER_SERVER_CHAT_LOG_PATH;
                break;
            default:
                //地面
                path = DstConstant.ROOT_PATH + DstConstant.SINGLE_SLASH + DstConstant.DST_MASTER_SERVER_LOG_PATH;
        }
        File file = new File(path);
        List<String> result = FileUtils.readLastNLine(file, rowNum);
        if (CollectionUtils.isEmpty(result)){
            result = Lists.newArrayList("未找到日志，日志的路径为:"+path);
        }
        return result;
    }

    /**
     * 获取任务时间列表
     * @return 数据
     */
    public ScheduleVO getScheduleList() {
        ScheduleVO data = new ScheduleVO();
        Set<String> updateSet = DstConfigData.SCHEDULE_UPDATE_MAP.keySet();
        if (CollectionUtils.isNotEmpty(updateSet)){
            List<ScheduleVO.InnerData> updateTimeList = new ArrayList<>();
            updateSet.forEach(e->{
                ScheduleVO.InnerData innerData = new ScheduleVO.InnerData();
                innerData.setTime(e);
                innerData.setCount(DstConfigData.SCHEDULE_UPDATE_MAP.get(e));
                updateTimeList.add(innerData);
            });
            data.setUpdateTimeList(updateTimeList);
        }
        Set<String> backupSet = DstConfigData.SCHEDULE_BACKUP_MAP.keySet();
        if (CollectionUtils.isNotEmpty(backupSet)){
            List<ScheduleVO.InnerData> backupTimeList = new ArrayList<>();
            backupSet.forEach(e->{
                ScheduleVO.InnerData innerData = new ScheduleVO.InnerData();
                innerData.setTime(e);
                innerData.setCount(DstConfigData.SCHEDULE_BACKUP_MAP.get(e));
                backupTimeList.add(innerData);
            });
            data.setBackupTimeList(backupTimeList);
        }
        data.setNotStartMaster(DstConfigData.notStartMaster);
        data.setNotStartCaves(DstConfigData.notStartCaves);
        return data;
    }

    /**
     * 将任务时间写入缓存中
     * @param vo 提交的数据
     */
    public void saveSchedule(ScheduleVO vo) {
        DstConfigData.clearAllData();
        if (CollectionUtils.isNotEmpty(vo.getBackupTimeList())){
            //写入缓存
            vo.getBackupTimeList().forEach(e->{
                if (StringUtils.isNotBlank(e.getTime())){
                    DateTime parse = DateUtil.parse(e.getTime(), DatePattern.NORM_DATETIME_MINUTE_PATTERN);
                    String format = DateUtil.format(parse, DatePattern.NORM_TIME_PATTERN);
                    DstConfigData.SCHEDULE_BACKUP_MAP.put(format,e.getCount());
                }
            });
        }
        if (CollectionUtils.isNotEmpty(vo.getUpdateTimeList())){
            //写入缓存
            vo.getUpdateTimeList().forEach(e->{
                if (StringUtils.isNotBlank(e.getTime())) {
                    DateTime parse = DateUtil.parse(e.getTime(), DatePattern.NORM_DATETIME_MINUTE_PATTERN);
                    String format = DateUtil.format(parse, DatePattern.NORM_TIME_PATTERN);
                    DstConfigData.SCHEDULE_UPDATE_MAP.put(format, e.getCount());
                }
            });
        }
        if (vo.getNotStartCaves() != null) {
            DstConfigData.notStartMaster = vo.getNotStartMaster();
        }else {
            DstConfigData.notStartMaster = false;
        }
        if (vo.getNotStartCaves() != null) {
            DstConfigData.notStartCaves = vo.getNotStartCaves();
        }else {
            DstConfigData.notStartCaves = false;
        }
    }
}
