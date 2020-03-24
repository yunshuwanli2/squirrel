package com.serial;

/**
 * 单片机返回数据，处理信息接口
 * @author sam
 *
 */
public interface Rs232Callback {
	
	/**
	 * 垃圾桶已开门
	 * @param number
	 */
	void onReceiveOpen(int number);

	/**
	 * 获取垃圾桶详细信息
	 * @param number		垃圾桶编号
	 * @param weight		重量，有小数点，单位kg
	 * @param temperature	温度
	 * @param smokeWarn		烟雾报警，01有报警，00表示正常
	 * @param fireWarn		灭火器状态，01有报警，00表示正常
	 * @param timeSet		时间状态，01开启，00关闭
	 * @param times			多个时间段，使用;分隔
	 */
	void onReceiveBordInfo(int number, String weight, int temperature, String smokeWarn, String fireWarn, String timeSet,
                           String times);
	/**
	 * 成功设置 垃圾桶至0，即清空,返回结果到后台
	 */
	void onReset(int number);
	/**
	 * 成功设置 垃圾桶零点校准，返回结果到后台
	 */
	void onReset0(int number);
	/**
	 * 成功设置 垃圾桶负载校准，返回结果到后台。
	 */
	void onResetWeight(int number);
	/**
	 * 成功设置时间返回标志
	 */
	void onSetTime(int number);
	/**
	 *
	 * @param weight 获取重量
	 */
	void onReceiveWeight(int number, String weight, String timeID);
	/**
	 * 火灾报警
	 */
	void onFireWarn(int number, String msg);
	/**
	 * 烟雾报警
	 */
	void onSmokeWarn(int number, String msg);
	/**
	 * 满载报警
	 */
	void onFullWarn(int number, String msg);
	/**
	 * 灭火器溶剂不足报警
	 */

	void onFireToolsEmptyWarn(int number, String msg);
	/**
	 * 电机故障报警
	 */
	void onMachineWarn(int number, String msg);

}
