package com.elnaz.brushbuddy.models

import kotlinx.serialization.descriptors.SerialDescriptor

enum class Models{
    D36,
    D701,
    D700,
    IOSeries,
    IOSeries4,
    IOSeries5,
    D21,
    D601,
    D706,
    Unknown;
    companion object {
        fun fromId(id: Int): Models {
            return when (id) {
                0, 1, 2 -> D36
                32, 33, 34 -> D701
                39, 40, 41 -> D700
                48, 49, 50, 54 -> IOSeries
                52 -> IOSeries4
                53 -> IOSeries5
                64, 65, 66, 67, 68, 69, 70 -> D21
                80, 81, 82, 83, 84, 85, 86, 87 -> D601
                112, 113, 114, 117, 118, 119 -> D706
                else -> Unknown
            }
        }
    }
}
enum class ToothBrushState( val id: Int, val description: String){
    UNKNOWN(0, "unknown"),
    INITIALIZING(1, "initializing"),
    IDLE(2, "idle"),
    RUNNING(3, "running"),
    CHARGING(4, "charging"),
    SETUP(5, "setup"),
    FLIGHT_MENU(6, "flight menu"),
    SELECTION_MENU(8, "selection menu"),
    OFF(9, "off"),
    POST_BRUSHING_STATISTICS(10, "post brushing statistics"),
    FINAL_TEST(113, "final test"),
    PCB_TEST(114, "pcb test"),
    SLEEPING(115, "sleeping"),
    TRANSPORT(116, "transport");
    companion object {
        fun fromId(id: Int) : ToothBrushState? {
            return entries.find{ it.id == id}
        }
    }
}
val SMART_SERIES_MODES: Map<Int, String> = mapOf(
    0 to "off",
    1 to "daily clean",
    2 to "sensitive",
    3 to "massage",
    4 to "whitening",
    5 to "deep clean",
    6 to "tongue cleaning",
    7 to "turbo",
    255 to "unknown"
)
val IO_SERIES_MODES:Map<Int, String> = mapOf(
    0 to "daily clean",
    1 to "sensitive",
    2 to "gum care",
    3 to "whiten",
    4 to "intense",
    5 to "super sensitive",
    6 to "tongue cleaning",
    8 to "settings",
    9 to "off",
    11 to "smart adapt",
)
val DEVICE_TYPES: Map<Models, ModelDescription> = mapOf(
    Models.D36 to ModelDescription("Triumph D36", SMART_SERIES_MODES),
    Models.D21 to ModelDescription("Smart Series D21", SMART_SERIES_MODES),
    Models.D601 to ModelDescription("Pro Series D601", SMART_SERIES_MODES),
    Models.D700 to ModelDescription("Smart Series D700", SMART_SERIES_MODES),
    Models.D701 to ModelDescription("Genius Series D701", SMART_SERIES_MODES),
    Models.D706 to ModelDescription("Genius X D706", SMART_SERIES_MODES),

    Models.IOSeries4 to ModelDescription("IO Series 4", IO_SERIES_MODES),
    Models.IOSeries5 to ModelDescription("IO Series 5", IO_SERIES_MODES),
    Models.IOSeries to ModelDescription("IO Series", IO_SERIES_MODES),

    Models.Unknown to ModelDescription("Unknown", SMART_SERIES_MODES)
)
/*
ModelDescription
│
├── deviceType : String
│
└── modes      : Map<Int, String>
 */
data class ModelDescription( val deviceType: String, val modes: Map<Int, String>)
class OralBAdvertisement(
    val data : ByteArray
) {
    init {
        require(data.size == 9 || data.size == 11)
        {
            "Invalid Oral-B advertisement length: ${data.size}"
        }
    }
    val msgLength = data.size

    val modelType = data[1].toInt() and 0xFF
    val state = data[3].toInt() and 0xFF
    val pressure = data[4].toInt() and 0xFF
    val brushingTimeSeconds =
        (data[5].toInt() and 0xFF) * 60 +
                (data[6].toInt() and 0xFF)
    val mode = data[7].toInt() and 0xFF
    val sector = data[8].toInt() and 0xFF

    val sectorTimer =
        if (msgLength == 11)
            data[9].toInt() and 0xFF
        else
            null

    val numberOfSectors =
        if (msgLength == 11)
            data[10].toInt() and 0xFF
        else
            null
    val model = Models.fromId(modelType)
    val toothBrushState = ToothBrushState.fromId(state)
    val modelInfo = DEVICE_TYPES.getValue(model)
//    val name = "${modelInfo.deviceType} ${shortAddress(address)}"
    val brushingStatus = if(toothBrushState == ToothBrushState.RUNNING){
    BrushingStatus.BRUSHING
    }else {
    BrushingStatus.IDLE
    }
}