# 第五周框架

> 老师你好，上周作业我以我对并发编程的总结作为作业内容，但作业被标注为未提交，可能是老师误以为是我拷贝别人的文章，下面是我的作业与原文地址，麻烦老师核实一下。 
> 作业 ：https://github.com/zhengyin/JAVA-000/tree/main/Week_04
> 原文 ：https://github.com/zhengyin/java-concurrent
> 另外本着学习为目的，不是完成作业为目的的原则，可能我的作业和老师布置的会有出入，但都是符合课程内容的，我希望以体系化的总结来加深对知识的理解，请老师见谅，如有不妥之处请指正。 



## 本次作业以一个完整的spring boot 缓存扩展作为作业 , 里面包括了老师课上讲的知识，包括AOP，自动配置，注解配置，缓存等。

https://github.com/zhengyin/spring-boot-cache-extend

> spring-boot-cache-extend  是对spring-cache的包装，提供在spring-boot中使用redis与caffeine缓存开箱即用的功能。

1. 统一的cache key 生成器

> 统一的cache key 有助于我们规范的管理缓存的key，特别是在neibu环境中共用同一redis时，避免缓存key重复:

缓存key命名规范
``` 
    key = cacheName :: application : targetClass . targetMethod : params
```

``` 
    如 : TTL_5::spring-boot-example-cache:TestController.hello:visitor 
```

2. @CacheTarget 注解

> @CacheTarget 是用于定义目标的缓存类，便于在清除缓存时可以随时在别处进行清除

3. 兼容所有的 spring-cache 功能

4. 缓存使用示例

https://github.com/zhengyin/spring-boot-example/blob/master/spring-boot-example-cache/src/test/java/com/izhengyin/springboot/example/cache/test/ApplicationTests.java

## 注意事项

### 1. 使用  @Cacheable(key = "#name") 自定义缓存key时不会使用统一的缓存生成器, 参考示例 customKeyTest

### 2. 使用  @Cacheable(keyGenerator = CacheKeyGeneratorConfig.MY_KEY_GENERATOR ) 自定义缓存key生成器时不会使用统一的缓存生成器, 参考示例 customKeyGeneratorTest

### 3. 由于 Aop 的特性，在类中使用 this 调用方法是不会触发 Aop 增强，因此缓存注解不会生效 . 参考此文章 https://www.ibm.com/developerworks/cn/opensource/os-cn-spring-cache/

