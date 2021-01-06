# 基于Redis封装分布式数据操作:

源码: https://github.com/zhengyin/spring-boot-example/blob/master/spring-boot-example-cache/src/test/java/com/izhengyin/springboot/example/cache/test/RedisOperationTest.java

1. 在Java中实现一个简单的分布式锁;

``` 
   /**
     * 分布式锁
     * @throws InterruptedException
     */
    @Test
    public void lock() throws InterruptedException{
        String key = "LOCK";
        AtomicInteger counter = new AtomicInteger();
        CountDownLatch downLatch = new CountDownLatch(3);
        //并发获取锁
        ExecutorService executors = Executors.newFixedThreadPool(3);
        IntStream.range(0,3).forEach(i -> executors.execute(() -> {
                    try {
                        //锁3秒超时
                        boolean obtainLock = Optional.ofNullable(stringRedisTemplate.opsForValue()
                                .setIfAbsent(key,"1",3, TimeUnit.SECONDS))
                                .orElse(false);
                        if(obtainLock){
                            counter.getAndIncrement();
                        }
                        try {
                            //模拟业务操作耗时
                            TimeUnit.SECONDS.sleep(1);
                        }catch (InterruptedException e){

                        }
                    }finally {
                        stringRedisTemplate.delete(key);
                        downLatch.countDown();
                    }
                })
        );
        downLatch.await();
        Assert.assertEquals(1,counter.get());
    }

```

2. 在Java中实现一个分布式计数器，模拟减库存。

```
  /**
     * 计数
     */
    @Test
    public void counter(){
        String inventoryKey = "inventory";
        String inventoryLockKey = "inventory-lock";
        stringRedisTemplate.opsForValue().set(inventoryKey,10+"");
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        IntStream.range(0,100)
                .forEach(i -> {
                    executorService.execute(() -> {
                        try {
                            //锁1秒超时
                            boolean obtainLock = Optional.ofNullable(stringRedisTemplate.opsForValue()
                                    .setIfAbsent("inventory-lock","1",3, TimeUnit.SECONDS))
                                    .orElse(false);
                            if(!obtainLock){
                                return;
                            }
                            int residue = Optional.ofNullable(stringRedisTemplate.opsForValue().get(inventoryKey)).map(Integer::parseInt).orElse(0);
                            if(residue > 0){
                                System.out.println("剩余库存 " + stringRedisTemplate.opsForValue().decrement(inventoryKey));
                            }
                        }finally {
                            stringRedisTemplate.delete(inventoryLockKey);
                        }
                    });
                });
    }
```

3. 基于Redis的PubSub实现订单异步处理

```
    @Test
    public void pubsub(){
        Stream.of(1,2,3,4,5)
                .map(i -> {
                    //每秒发送一个
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    }catch (InterruptedException e){}
                    return "order id  "+i;
                })
                .forEach(msg -> {
                    stringRedisTemplate.convertAndSend(RedisMessageListenerConfiguration.TOPIC,msg);
                });
    }

    @Configuration
    public class RedisMessageListenerConfiguration {
        public final static String TOPIC  = "testTopic";
    
        @Bean
        MessageListenerAdapter messageListenerAdapter() {
            return new MessageListenerAdapter((MessageListener)(message,bytes) -> {
                String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
                String body = new String(message.getBody(), StandardCharsets.UTF_8);
                System.out.println("received  [ "+channel +"]  message [ "+body+" ]");
            });
        }
    
        @Bean
        RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
            RedisMessageListenerContainer container = new RedisMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.addMessageListener(listenerAdapter, new PatternTopic(TOPIC));
            return container;
        }
    }


```


# (选做)spring集成练习 （扩展Spring-boot-cache）

源码 ： https://github.com/zhengyin/spring-boot-cache-extend

# spring-boot-cache-extend 


> spring-boot-cache-extend  是对spring-cache的包装，提供在spring-boot中使用redis与caffeine缓存开箱即用的功能。

## 接入步骤

1.添加maven依赖 

 mvn install

``` 
   <dependency>
        <groupId>com.izhengyin</groupId>
        <artifactId>spring-boot-cache-extend</artifactId>
        <version>0.1.0</version>
    </dependency>
```

2. 配置Redis

``` 
redis.cache.host = 127.0.0.1
redis.cache.port = 6379
redis.cache.database = 0
redis.cache.timeout = 1000ms
redis.cache.password =
redis.cache.jedis.pool.max-active = 32
redis.cache.jedis.pool.max-wait = 3000ms
redis.cache.jedis.pool.max-idle = 10
redis.cache.jedis.pool.min-idle = 1
```

## spring-boot-cache-extend 提供了什么？

1. 统一的cache name

> 在spring-cache中想要对缓存单独的制定缓存时间需要预先配置，在 spring-boot-cache-extend 中预先定义了如下的缓存名与TTL对应 , Caffeine为内存缓存，为了避免内存溢出，因此ttl定义时间都不长，且每一个cacheName最多容纳10000个key，请酌情使用


``` 
package com.kongfz.util.cache.constant;
public class CacheName {
    /**
     * Redis 缓存
     */
    public interface Redis {
        String TTL_1 = "TTL_1";
        String TTL_5 = "TTL_5";
        String TTL_60 = "TTL_60";
        String TTL_180 = "TTL_180";
        String TTL_300 = "TTL_300";
        String TTL_600 = "TTL_600";
        String TTL_1800 = "TTL_1800";
        String TTL_3600 = "TTL_3600";
        String TTL_10800 = "TTL_10800";
        String TTL_21600 = "TTL_21600";
        String TTL_43200 = "TTL_43200";
        String TTL_86400 = "TTL_86400";
        String TTL_3_DAY = "TTL_3_DAY";
        String TTL_7_DAY = "TTL_7_DAY";
    }
    /**
     * CAFFEINE 本地缓存
     */
    public interface Caffeine{
        String TTL_1 = "TTL_1";
        String TTL_5 = "TTL_5";
        String TTL_60 = "TTL_60";
        String TTL_300 = "TTL_300";
        String TTL_600 = "TTL_600";
    }
}
    
```

2. 统一的cache key 生成器

> 统一的cache key 有助于我们规范的管理缓存的key，特别是在neibu环境中共用同一redis时，避免缓存key重复:

缓存key命名规则
``` 
    key = cacheName :: application : targetClass . targetMethod : params
```

``` 
    如 : TTL_5::spring-boot-example-cache:TestController.hello:visitor 
```

3. @CacheTarget 注解

> @CacheTarget 是用于定义目标的缓存类，便于在清除缓存时可以随时在别处进行清除; 比如你可以在 service 层清除controller定义的缓存

``` 
Controller ...
    @Cacheable(cacheNames = {CacheName.Redis.TTL_5} , keyGenerator = GeneratorName.DEFAULT , cacheManager = CacheDrive.REDIS)
    @GetMapping("/hello/{name}")
    public Object hello(@PathVariable String name){
        return "Hello "+name+" , I am "+application +" , time "+System.currentTimeMillis();
    }
Service ...
    @CacheTarget(TestController.class)
    @CacheEvict(cacheNames = CacheName.Redis.TTL_5 , keyGenerator = GeneratorName.DEFAULT , cacheManager = CacheDrive.REDIS)
    public void hello(String name){}
```


4. 兼容所有的 spring-cache 功能

5. 缓存使用示例 (一定要看)

https://github.com/zhengyin/spring-boot-example/blob/master/spring-boot-example-cache/src/test/java/com/izhengyin/springboot/example/cache/test/ApplicationTests.java

## 注意事项

1. 使用  @Cacheable(key = "#name") 自定义缓存key时不会使用统一的缓存生成器, 参考示例 customKeyTest

2. 使用  @Cacheable(keyGenerator = CacheKeyGeneratorConfig.MY_KEY_GENERATOR ) 自定义缓存key生成器时不会使用统一的缓存生成器, 参考示例 customKeyGeneratorTest

3. 由于 Aop 的特性，在类中使用 this 调用方法是不会触发 Aop 增强，因此缓存注解不会生效 . 参考此文章 https://www.ibm.com/developerworks/cn/opensource/os-cn-spring-cache/