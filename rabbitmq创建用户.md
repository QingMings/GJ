通过 guest 只能本地访问 localhost:15672/，并不能实现远程访问，
所以当远程访问时，需要添加新用户才能访问。
1、进入 RabbitMQ 的 sbin 目录，打开 cmd
2、输入 `rabbitmqctl add_user myRoot 123`
创建一个叫 myRoot 密码 123 的用户
3、输入 `rabbitmqctl set_user_tags myRoot administrator` 设置用户为管理员
4、输入 `rabbitmqctl add_vhost VHOST` 创建一个叫 VHOST 的虚拟用户目录
5、`rabbitmqctl set_permissions -p VHOST myRoot ".*" ".*" ".*"` 为用户设置权限

	到这里，就可以通过 目标ip:15672/ 访问远程 rabbitmq 了
