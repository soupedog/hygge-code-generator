# 简介

这是一个代码生成器，能连接数据库拉取表信息，并在您当前项目中添加表对应 `PO(Persistent Object)` 的 `Java` 代码。

# 源码相关

## 源码浅析

- 本项目定义了一套 `Java` 类的描述规范，参见 `hygge.util.generator.java.bo.ClassInfo`
- 类生成器会根据上述规范生成 `Java` 普通类、基类和枚举类，生成规则是纯粹的字符串拼接，参见 `hygge.util.generator.java.JavaGenerator`
- 插件会连接数据库，并拉取相关数据，参见 `hygge.plugin.generator.core.service.topo.MysqlDatabaseInfoScanner`
- 插件会将读取到的数据库信息转化成 `ClassInfo` 并交由类生成器进而在当前项目中生成 `Java` 代码，参见 `hygge.plugin.generator.component.toolWindow.HyggeGeneratorToolWindowFactory`

## 快速开始

通过 `IDEA` 克隆该项目到本地 → `Run/Debug Configurations` → `+` → `Gradle` → `Run` 中填写 `runIde` → `OK`

随后运行项目即可启动插件在本地进行调试。

# 使用方法

## 支持的 IDEA

`IC-222.4554.10` 及以上

## 支持的数据库

- MySQL

## 类型转换相关

### MySQL 类型对照表

| Java 类型       | 数据库类型                      |
|---------------|----------------------------|
| `Boolean`     | `tinyint(1)`               |
| `Byte`        | `tinyint` 但不为 `tinyint(1)` |
| `Short`       | `smallint`                 |
| `Integer`     | `mediumint`、`int`          |
| `Long`        | `bigint`                   |
| `Float`       | `float`                    |
| `Double`      | `double`                   |
| `BigDecimal`  | `decimal`                  |
| `配置项中的默认时间类型` | `datetime`、`timestamp`     |
| `自动生成的枚举类`    | `enum`                     |
| `String`      | 上述未提到的类型均转换成 `String` 类型   |

## 样例

```sql
create table user_info
(
	uid bigint auto_increment comment '自增唯一标识'
		primary key,
	level int null comment '账号等级',
	name varchar(100) not null comment '姓名',
	age tinyint default 0 null comment '年龄',
	sex enum('FEMALE', 'MALE', 'SECRET') default 'SECRET' not null comment '性别:女性,男性,保密',
	is_married tinyint(1) default 0 not null comment '是否已婚',
	balance decimal(12,2) default 0.00 not null comment '余额',
	create_ts timestamp null comment '创建时间戳',
	last_update_ts datetime null comment '最后修改时间戳'
)
comment '用户信息';
```

勾选 `Lombok`、`Underscore To Camel Case`，其余配置项保持默认，将生成的代码为：

```java
package hygge.domain.enums;

import lombok.Getter;

/**
 * 性别
 *
 * @author Hygge Generator
 * @date 2024-06-19
 */
@Getter
public enum SexEnum {
    /**
     * 女性
     */
    FEMALE(0, "FEMALE"),
    /**
     * 男性
     */
    MALE(1, "MALE"),
    /**
     * 保密
     */
    SECRET(2, "SECRET"),
    ;

    SexEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 枚举数字值
     */
    private final Integer value;
    /**
     * 枚举文本值
     */
    private final String text;

    /**
     * 根据数字获取对应的枚举
     *
     * @throws IllegalArgumentException 当入参为 null 或与枚举匹配失败时
     */
    public static SexEnum parse(Integer target) {
        if (target == null) {
            throw new IllegalArgumentException("Unexpected value of SexEnum, it can't be null.");
        }
        switch (target) {
            case 0:
                return SexEnum.FEMALE;
            case 1:
                return SexEnum.MALE;
            case 2:
                return SexEnum.SECRET;
            default:
                throw new IllegalArgumentException("Unexpected value of SexEnum, it can't be " + target + ".");
        }
    }

    /**
     * 根据数字获取对应的枚举
     *
     * @param defaultValue 入参为 null 或转换发生异常时，强制返回的转换结果
     */
    public static SexEnum parse(Integer target, SexEnum defaultValue) {
        if (target == null) {
            return defaultValue;
        }
        switch (target) {
            case 0:
                return SexEnum.FEMALE;
            case 1:
                return SexEnum.MALE;
            case 2:
                return SexEnum.SECRET;
            default:
                return defaultValue;
        }
    }

    /**
     * 根据枚举 text 或 name 值获取对应的枚举
     *
     * @throws IllegalArgumentException 当入参为 null 或与枚举匹配失败时
     */
    public static SexEnum parse(String target) {
        if (target == null) {
            return SexEnum.valueOf(null);
        }
        switch (target) {
            case "FEMALE":
                return SexEnum.FEMALE;
            case "MALE":
                return SexEnum.MALE;
            case "SECRET":
                return SexEnum.SECRET;
            default:
                return SexEnum.valueOf(target);
        }
    }

    /**
     * 根据枚举 text 或 name 值获取对应的枚举
     *
     * @param defaultValue 入参为 null 或转换发生异常时，强制返回的转换结果
     */
    public static SexEnum parse(String target, SexEnum defaultValue) {
        if (target == null) {
            return defaultValue;
        }
        switch (target) {
            case "FEMALE":
                return SexEnum.FEMALE;
            case "MALE":
                return SexEnum.MALE;
            case "SECRET":
                return SexEnum.SECRET;
            default:
                try {
                    return SexEnum.valueOf(target);
                } catch (IllegalArgumentException e) {
                    return defaultValue;
                }
        }
    }
}
```

```java
package hygge.domain.po;

import hygge.domain.enums.SexEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * 用户信息
 *
 * @author Hygge Generator
 * @date 2024-06-19
 */
@Getter
@Setter
@Builder
@Generated
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    /**
     * 自增唯一标识
     */
    private Long uid;
    /**
     * 账号等级
     */
    private Integer level;
    /**
     * 姓名
     */
    private String name;
    /**
     * 年龄
     */
    private Byte age;
    /**
     * 性别:女性,男性,保密
     */
    private SexEnum sex;
    /**
     * 是否已婚
     */
    private Boolean isMarried;
    /**
     * 余额
     */
    private BigDecimal balance;
    /**
     * 创建时间戳
     */
    private ZonedDateTime createTs;
    /**
     * 最后修改时间戳
     */
    private ZonedDateTime lastUpdateTs;
}

```

## 自定义功能

自定义功能要求用户传入 `Java` 类的描述规范进而实现特殊功能，请务必确保自己对这套规范有一定了解，常见类的描述示例参见 `hygge.util.constant.ConstantClassInfoContainer`

### 自定义基类

典型应用场景：

数据库表均有 `createTs`、`lastUpdateTs` 字段，不希望这两个属性弥散在各个 `PO` 之中，而希望出现一个包含这两个属性的基类，并让各个拥有这两个属性的 `PO` 自动继承这个基类。

操作方法：

向 `Base Class Info` 传入类描述信息，它接收的是一个 `ClassInfo` 数组的 `Json` 形式，因继承有排他性，故 `PO` 只会继承与自己有相同属性最多的那个基类。

#### 普通基类示例

```json
{
  "name": "BasePo",
  "modifiers": [
    "PUBLIC",
    "ABSTRACT"
  ],
  "type": "DEFAULT_CLASS",
  "description": "PO 对象基类",
  "annotations": [],
  "references": [],
  "properties": [
    {
      "name": "createTs",
      "classInfo": {
        "packageInfo": "java.time",
        "name": "OffsetDateTime",
        "modifiers": [
          "PUBLIC"
        ],
        "type": "DEFAULT_CLASS",
        "annotations": [],
        "references": [],
        "properties": [],
        "enumElements": []
      },
      "description": "创建 UTC 毫秒级时间戳",
      "annotations": [],
      "modifiers": [
        "PROTECTED"
      ]
    },
    {
      "name": "lastUpdateTs",
      "classInfo": {
        "packageInfo": "java.time",
        "name": "OffsetDateTime",
        "modifiers": [
          "PUBLIC"
        ],
        "type": "DEFAULT_CLASS",
        "annotations": [],
        "references": [],
        "properties": [],
        "enumElements": []
      },
      "description": "最后修改 UTC 毫秒级时间戳",
      "annotations": [],
      "modifiers": [
        "PROTECTED"
      ]
    }
  ],
  "enumElements": []
}
```

#### 开启 Lombok 的基类示例

```json
{
  "name": "BasePo",
  "modifiers": [
    "PUBLIC",
    "ABSTRACT"
  ],
  "type": "DEFAULT_CLASS",
  "description": "PO 对象基类",
  "annotations": [
    {
      "packageInfo": "lombok",
      "name": "Getter",
      "modifiers": [
        "PUBLIC"
      ],
      "type": "ANNOTATION",
      "annotations": [],
      "references": [],
      "properties": [],
      "enumElements": []
    },
    {
      "packageInfo": "lombok",
      "name": "Setter",
      "modifiers": [
        "PUBLIC"
      ],
      "type": "ANNOTATION",
      "annotations": [],
      "references": [],
      "properties": [],
      "enumElements": []
    },
    {
      "packageInfo": "lombok",
      "name": "Generated",
      "modifiers": [
        "PUBLIC"
      ],
      "type": "ANNOTATION",
      "annotations": [],
      "references": [],
      "properties": [],
      "enumElements": []
    },
    {
      "packageInfo": "lombok",
      "name": "NoArgsConstructor",
      "modifiers": [
        "PUBLIC"
      ],
      "type": "ANNOTATION",
      "annotations": [],
      "references": [],
      "properties": [],
      "enumElements": []
    }
  ],
  "references": [],
  "properties": [
    {
      "name": "createTs",
      "classInfo": {
        "packageInfo": "java.time",
        "name": "OffsetDateTime",
        "modifiers": [
          "PUBLIC"
        ],
        "type": "DEFAULT_CLASS",
        "annotations": [],
        "references": [],
        "properties": [],
        "enumElements": []
      },
      "description": "创建 UTC 毫秒级时间戳",
      "annotations": [],
      "modifiers": [
        "PROTECTED"
      ]
    },
    {
      "name": "lastUpdateTs",
      "classInfo": {
        "packageInfo": "java.time",
        "name": "OffsetDateTime",
        "modifiers": [
          "PUBLIC"
        ],
        "type": "DEFAULT_CLASS",
        "annotations": [],
        "references": [],
        "properties": [],
        "enumElements": []
      },
      "description": "最后修改 UTC 毫秒级时间戳",
      "annotations": [],
      "modifiers": [
        "PROTECTED"
      ]
    }
  ],
  "enumElements": []
}
```