1. BigTableConfig.xsd ���ڷֿ�ֱ��һ�����á���ѯ�������ֿ�ֱ���ֶΡ�
2. CDO.xsd  �Ƕ����˿����Ҫ�õ��Ļ����������ͣ��������� �� ��������CDO,CDO����
3. DataService.xsd ������ һ���׹�ϵ�����ݿ�SQL �� �ĵ����ݿ�MongoDB��д��

4. Framewok.xsd �� FrameworkResource.xsd ��  FiltersConfig.xsd ���,�ֲ���Ҫ��Ϊ�˲��𷽱㣬������;����仯�ķֿ���
   FrameworkResource.xsd ��Ҫ���������ڷֲ�ʽ���� memcache������Դ
   FiltersConfig.xsd  ������ʹ�û�����������  

5  ServiceBus.xsd �� ServiceBusResource.xsd  �� PluginsConfig.xsd ��ɣ��ֲ���Ҫ��Ϊ�˲��𷽱㣬������;����仯�ķֿ���
   ServiceBusResource.xsd ��Ҫ������ ��ϵ�����ݿ�Դ�� �ĵ����ݿ�MongodbԴ������,�¼�����
   PluginsConfig.xsd   ������ʹ�ò��������

6  TransFilter ������ ʹ��ǰ���¼��������¼����������õ�д����



7 ���������Ǽ��׹�ϵͼ�����治�Ǳ����.��ʹ�û������Ҫ�ֲ�ʽ����memcache,�������ã�Ҳ��ͨ��java����ʵ�ֵ���CacheHandlerTemplate
  ʵ��,TransFilter��ʹ����ͨplugin.xml���TransService
   

                                                                                        
                                                                                       
              ��ServiceBusResource[���ö�����ݿ�Դ]                                 __frameworkResource[�����ö��memcacheԴ��]
             |                                                                      |                     
             |                 	������������Plugin.xml[���ò��� ����SystemService]��|__filtersConfig---���TransFilter                  
ServiceBus---|                 |                    
             |                 |                                       
              ��PluginsConfig��|
                               |                          __���TransService  
                               |                         |   
                                ���������ͨplugin.xml---|
                                                         |__���DataService
