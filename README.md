# tijos-gizwits-mcu-wifi
An application for Gizwits IoT cloud based on TiJOS, it's applicable for standalone MCU solution provided by Gizwits.



# 基于钛极OS(TiJOS)的机智云接入案例

## 简介

机智云Giz(mo)Wits（设备智慧）是广州机智云物联网科技有限公司旗下品牌，全球领先的物联网开发平台和物联网云服务商，全栈（综合）物联网平台服务领导者，2014年推出中国第一个智能硬件自助开发(PaaS)及云服务(SaaS)平台，是中国最大的物联网开发平台。

目前机智云通过自动生成MCU代码的方式大大简化了设备接入的难度，但同时也对想要进行更多复杂度应用开发人员提出了更高的要求，钛极OS(TiJOS)的机智云接入方案从根本上解决这个问题， 用户在系统中定义数据点后，直接在钛极OS(TiJOS)Java代码中进行简单设置即可，无需对硬件底层次了解过多， 即可应对复杂应用。

该安全基于TiKit-T800-STM32F103A型号及传感器DEMO板，基于机智云ESP8266模块的MCU模式。



## 注册机智云

在测试之前请先注册机智云开发者帐号，具体请参考 https://dev.gizwits.com/zh-cn/developer/



## 新建产品

在机智云的开发者平台中新建产品,注意ProductKey和Product Secret将会在代码中用到

![1524896161184](.\img\product.png)



## 定义数据点

定义产品后， 需要为产品定义数据点，可将例程中TiKit_T800.xlsx定义的数据点导入即可

![1524896263130](.\img\datapoint.png)

##钛极OS(TiJOS)应用开发
接入来即可在TiStudio中进行机智云接入的开发了， 在新建TiJOS应用工程后，将gizwits.jar加入到工程中，将产品的ProductKey和Product Secret填加到代码并定义相应的数据点

### 初始化机智云设备

```java
//从机智云产品信息中获得
String productKey = "e042b9f38c14498bbd8c9bbcead15d2f";
String productSecret = "ccd1f2d169e9498db0f6878d14e3034b";

System.out.println("Gizwits Demo Start ...");

//定义机智云设备
GizwitsDevice giz = new GizwitsDevice(productKey, productSecret, 4/*数据点个数*/);

```



### 定义数据点

根据机智云中的设置进行设置即可

```java
//数据点管理器
DataPointManager dpm = giz.getDPM();

//根据数据点定义设置每一个数据点的相应属性
dpm.setDataPoint(0, new DataPoint("RelayControl", DataType.typeBool, DataGroup.readWrite, 1));

dpm.setDataPoint(1, new DataPoint("Temperature", DataType.typeUint8, DataGroup.readOnly, 1, 0));

dpm.setDataPoint(2, new DataPoint("Huminity", DataType.typeUint8, DataGroup.readOnly, 1, 0));

dpm.setDataPoint(3, new DataPoint("LEDControl", DataType.typeBool, DataGroup.readWrite, 1));

```



### 设置事件回调

定义事件处理，当WIFI变化或有控制命令来自机智云时会触发相应的事件

``` java
//当前请求从机智云下发时，相关事件会触发
giz.setEventListener(new GizwitsEventHandler(t800));
giz.initialize(in, out);
```

事件处理可参考相应的例程



### 更新点的值并上报机智云

当数据点相关的传感器数据发生变化时， 可通过相关的接口进行上报

```java
/**
 * 设置相应的点的值 
 */
dpm.setPointValue("Temperature", t800.getTemperature());
dpm.setPointValue("Huminity", t800.getHumidity());

//如果点的值有变化会上传至机智云
giz.deviceStatusUpdate();
```



## 总结

以上为钛极OS(TiJOS)接入机智云的基本流程，用户可通过TiKit-T800-F103A及配套的传感器DEMO板来轻松地进行相关功能的测试和开发， 具体请访问钛极OS(TiJOS)官网 www.tijos.net