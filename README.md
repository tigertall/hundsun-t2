hundsun-t2
## 介绍
基于t2sdk jmeter压力测试插件，对接t2使用 jresplus-t2 来支持。

## 部署
复制插件和依赖包到 apache-jmeter-XXX\lib\ext 目录下， Release 可以下载打包好的插件和依赖压缩包。

## 安装依赖
插件的依赖都可以通过jresplus-t2sdk自身的依赖关联到，所以pom中没有体现。

另外t2sdk实际依赖了org.springframework.core，但是pom中没有写，所以除了pom的递归依赖外，还需要下面这个包。
org.springframework.core-3.2.5.RELEASE.jar。

导出的依赖如下

```pom
commons-codec-1.4.jar
commons-io-2.4.jar
commons-lang3-3.7.jar
dom4j-1.6.1.jar
jackson-annotations-2.9.3.jar
jackson-core-2.9.3.jar
jackson-databind-2.9.3.jar
jresplus-context-api-1.2.3.jar
jresplus-context-core-1.2.3.jar
jresplus-t2sdk-api-1.2.13.jar
jresplus-t2sdk-core-1.2.13.jar
jresplus-t2sdk-spi-hs-1.2.13.jar
org.springframework.core-3.2.5.RELEASE.jar
servlet-api-2.5.jar
slf4j-api-1.7.9.jar
xml-apis-1.0.b2.jar
```

## 使用说明
包含两个组件，T2设置和T2Sampler。
### T2 设置
路径：`TestPlan -> Add -> Config Element-> T2设置`

只需要全局设置一个。

配置文件:选择t2sdk-config.xml，配置内容参考T2 JAVA SDK配置说明。

保持T2连接：默认不选。如果选择是，一轮测试执行完成后就不会断开T2连接。由于建立T2连接比较慢，这个保持后可以避免后续测试每次建连接的开销，在测试脚本和验证时比较有用。

### T2 Sampler
路径：`ThreadGroup -> Add -> Sampler -> T2Sampler`

保存应答：选择后会保存请求和应答信息，查看结果树中可以看到请求和应答内容。

特别的，对于应答返回错误的请求，一定会保存请求和应答信息。

## 其他说明
- 找不到对应的客户端

参考T2SDK JAVA版开发手册，由于sdk本身已经支持连接等待，所以插件中已经取消了连接建立的等待时间，如果出现上面报错需要通过 connectionWaitTimes 来调节。

> 在网速较慢的场景下(例如ping大于50ms)，如何使用JAVA T2SDK?
>
> 网速较慢且不稳定的情况下，JAVA T2SDK初始化连接和注册需要较长的时间。调用者可以通过设置performance-> connectionWaitTimes来增加注册的等待时间，该参数设置为整型值，等待时间= connectionWaitTimes*1.5秒，connectionWaitTimes默认为1。


-  日志记录

插件实现了日志接口，如果要记录心跳、连接建立等信息，在 t2sdk-config.xml中  配置 `logAdapter`：

> `<logAdapter className="com.hundsun.jmeter.protocol.t2.sampler.T2SamplerLog" />`

- Hundsun-t2.jar 的迁移

之前提供过一个 Hundsun-t2.jar 的包，这个包有写死等1s的处理，另外初始化也会有重复处理，可以替换为这个新的插件包。 

## 问题反馈
https://github.com/tigertall/hundsun-t2