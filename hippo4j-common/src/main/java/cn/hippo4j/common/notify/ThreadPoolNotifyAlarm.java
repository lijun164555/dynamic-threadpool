package cn.hippo4j.common.notify;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Thread pool notify alarm.
 *
 * @author chen.ma
 * @date 2021/8/15 19:13
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class ThreadPoolNotifyAlarm {

    /**
     * isAlarm
     */
    @NonNull
    private Boolean isAlarm;

    /**
     * activeAlarm
     */
    @NonNull
    private Integer activeAlarm;

    /**
     * capacityAlarm
     */
    @NonNull
    private Integer capacityAlarm;

    /**
     * interval
     */
    private Integer interval;

    /**
     * receives
     */
    private Map<String, String> receives;

}
