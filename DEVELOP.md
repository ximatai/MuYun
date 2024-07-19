# 开发文档

## 数据库

```shell
docker run -d \
   --name db-for-muyun \
   -e POSTGRES_PASSWORD=muyun2024 \
   -e POSTGRES_DB=muyun \
   -e PGDATA=/var/lib/postgresql/data/pgdata \
   -e TZ=Asia/Shanghai \
   -e PGTZ=Asia/Shanghai \
   -e LANG=en_US.UTF-8 \
   -p 54324:5432 \
   postgres:16
```
