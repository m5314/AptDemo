# AptDemo
学习注解编写的demo，简单实现butterknife中通过注解绑定Activity中的View

demo使用的Gradle版本是5.1.1，在学习注解相关知识和参考网上实现案例后，动手编写出这个demo。一开始发现自定义的注解处理器并没有执行，
运行网上别人的demo却没有问题，将网上的demo的Gradle版本升级到5.1.1后同样注解处理器失效，后面发现升级到Gradle5.0后需要在注解处理器
module中增加annotationProcessor 'com.google.auto.service:auto-service:1.0-rc5'依赖
