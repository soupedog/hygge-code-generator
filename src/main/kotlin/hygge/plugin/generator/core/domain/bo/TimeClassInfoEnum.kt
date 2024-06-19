package hygge.plugin.generator.core.domain.bo

import hygge.util.generator.java.bo.ClassInfo

enum class TimeClassInfoEnum(val classInfo: ClassInfo) {
    TIMESTAMP(hygge.util.constant.ConstantClassInfoContainer.TIMESTAMP),
    LOCAL_DATE_TIME(hygge.util.constant.ConstantClassInfoContainer.LOCAL_DATE_TIME),
    ZONED_DATE_TIME(hygge.util.constant.ConstantClassInfoContainer.ZONED_DATE_TIME),
    OFFSET_DATE_TIME(hygge.util.constant.ConstantClassInfoContainer.OFFSET_DATE_TIME),
}