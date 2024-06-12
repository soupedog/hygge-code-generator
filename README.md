- 需要自己创建一个 gradle configuration `Run Plugin`，运行参数填 `runIde`


```json
{
  "name" : "BasePo",
  "modifiers" : [ "PUBLIC", "ABSTRACT" ],
  "type" : "DEFAULT_CLASS",
  "description" : "PO 对象基类",
  "annotations" : [ {
    "packageInfo" : "lombok",
    "name" : "Getter",
    "modifiers" : [ "PUBLIC" ],
    "type" : "ANNOTATION",
    "annotations" : [ ],
    "references" : [ ],
    "properties" : [ ],
    "enumElements" : [ ]
  }, {
    "packageInfo" : "lombok",
    "name" : "Setter",
    "modifiers" : [ "PUBLIC" ],
    "type" : "ANNOTATION",
    "annotations" : [ ],
    "references" : [ ],
    "properties" : [ ],
    "enumElements" : [ ]
  }, {
    "packageInfo" : "lombok",
    "name" : "Generated",
    "modifiers" : [ "PUBLIC" ],
    "type" : "ANNOTATION",
    "annotations" : [ ],
    "references" : [ ],
    "properties" : [ ],
    "enumElements" : [ ]
  }, {
    "packageInfo" : "lombok",
    "name" : "NoArgsConstructor",
    "modifiers" : [ "PUBLIC" ],
    "type" : "ANNOTATION",
    "annotations" : [ ],
    "references" : [ ],
    "properties" : [ ],
    "enumElements" : [ ]
  } ],
  "references" : [ ],
  "properties" : [ {
    "name" : "createTs",
    "classInfo" : {
      "packageInfo" : "java.time",
      "name" : "OffsetDateTime",
      "modifiers" : [ "PUBLIC" ],
      "type" : "DEFAULT_CLASS",
      "annotations" : [ ],
      "references" : [ ],
      "properties" : [ ],
      "enumElements" : [ ]
    },
    "description" : "创建 UTC 毫秒级时间戳",
    "annotations" : [ ],
    "modifiers" : [ "PROTECTED" ]
  }, {
    "name" : "lastUpdateTs",
    "classInfo" : {
      "packageInfo" : "java.time",
      "name" : "OffsetDateTime",
      "modifiers" : [ "PUBLIC" ],
      "type" : "DEFAULT_CLASS",
      "annotations" : [ ],
      "references" : [ ],
      "properties" : [ ],
      "enumElements" : [ ]
    },
    "description" : "最后修改 UTC 毫秒级时间戳",
    "annotations" : [ ],
    "modifiers" : [ "PROTECTED" ]
  } ],
  "enumElements" : [ ]
}
```


```json
{
  "name" : "BasePo",
  "modifiers" : [ "PUBLIC", "ABSTRACT" ],
  "type" : "DEFAULT_CLASS",
  "description" : "PO 对象基类",
  "annotations" : [ ],
  "references" : [ ],
  "properties" : [ {
    "name" : "createTs",
    "classInfo" : {
      "packageInfo" : "java.time",
      "name" : "OffsetDateTime",
      "modifiers" : [ "PUBLIC" ],
      "type" : "DEFAULT_CLASS",
      "annotations" : [ ],
      "references" : [ ],
      "properties" : [ ],
      "enumElements" : [ ]
    },
    "description" : "创建 UTC 毫秒级时间戳",
    "annotations" : [ ],
    "modifiers" : [ "PROTECTED" ]
  }, {
    "name" : "lastUpdateTs",
    "classInfo" : {
      "packageInfo" : "java.time",
      "name" : "OffsetDateTime",
      "modifiers" : [ "PUBLIC" ],
      "type" : "DEFAULT_CLASS",
      "annotations" : [ ],
      "references" : [ ],
      "properties" : [ ],
      "enumElements" : [ ]
    },
    "description" : "最后修改 UTC 毫秒级时间戳",
    "annotations" : [ ],
    "modifiers" : [ "PROTECTED" ]
  } ],
  "enumElements" : [ ]
}
```