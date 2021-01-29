1. BigTableConfig.xsd 用于分库分表的一个配置。查询需以赖分库分表的字段。
2. CDO.xsd  是定义了框架需要用到的基础数据类型，数组类型 及 复合类型CDO,CDO数组
3. DataService.xsd 定义了 一整套关系型数据库SQL 和 文档数据库MongoDB的写法

4. Framewok.xsd 由 FrameworkResource.xsd 和  FiltersConfig.xsd 组成,分拆主要是为了部署方便，将不变和经常变化的分开。
   FrameworkResource.xsd 主要定义了用于分布式缓存 memcache的数据源
   FiltersConfig.xsd  定义了使用缓存插件的配置  

5  ServiceBus.xsd 由 ServiceBusResource.xsd  和 PluginsConfig.xsd 组成，分拆主要是为了部署方便，将不变和经常变化的分开。
   ServiceBusResource.xsd 主要定义了 关系型数据库源和 文档数据库Mongodb源的配置,事件启动
   PluginsConfig.xsd   定义了使用插件的配置

6  TransFilter 定义了 使用前置事件，后置事件，缓存配置的写法。



7 下面是他们简易关系图，缓存不是必须的.若使用缓存必须要分布式缓存memcache,不想配置，也可通过java代码实现调用CacheHandlerTemplate
  实现,TransFilter可使用普通plugin.xml里的TransService
   

                                                                                        
                                                                                       
              —ServiceBusResource[配置多个数据库源]                                 __frameworkResource[可配置多个memcache源组]
             |                                                                      |                     
             |                 	——单个缓存Plugin.xml[配置参数 调用SystemService]—|__filtersConfig---多个TransFilter                  
ServiceBus---|                 |                    
             |                 |                                       
              —PluginsConfig—|
                               |                          __多个TransService  
                               |                         |   
                                ——多个普通plugin.xml---|
                                                         |__多个DataService
