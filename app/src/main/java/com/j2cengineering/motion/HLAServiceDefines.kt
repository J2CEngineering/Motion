package com.j2cengineering.motion

import android.os.ParcelUuid


object HLAServiceDefines {

 const val DeviceInformationUUIDString = "0000180A-0000-1000-8000-00805F9B34FB"
 const val ManufactureNameUUIDString = "00002A29-0000-1000-8000-00805F9B34FB"
 const val ModelNumberUUIDString = "00002A24-0000-1000-8000-00805F9B34FB"
 const val SerialNumberUUIDString = "00002A25-0000-1000-8000-00805F9B34FB"
 const val HardwareRevisionUUIDString = "00002A27-0000-1000-8000-00805F9B34FB"
 const val FirmwareRevisionUUIDString = "00002A26-0000-1000-8000-00805F9B34FB"
 const val SoftwareRevisionUUIDString = "00002A28-0000-1000-8000-00805F9B34FB"

 const val HydroThermalLinearActuatorServieUUIDString = "C699F7DA-4371-4C54-98F9-C01D914F3721"
 const val HLACylinderPercentageUUIDString = "C6993763-4371-4C54-98F9-C01D914F3721"
 const val HLACommandUUIDString = "C699B282-4371-4C54-98F9-C01D914F3721"
 const val HLAStatusUUIDString = "C69994FD-4371-4C54-98F9-C01D914F3721"
 const val HLADataTransferUUIDString = "C699D3A1-4371-4C54-98F9-C01D914F3721"
 const val HLADeviceNameUUIDString = "C6995760-4371-4C54-98F9-C01D914F3721"
 const val HLACylinderPositionUUIDString = "C699446D-4371-4C54-98F9-C01D914F3721"
 const val HLAAccelerometerDataUUIDString = "C6999E6E-4371-4C54-98F9-C01D914F3721"
 const val HLACylinderTimeUUIDString = "C6999E6F-4371-4C54-98F9-C01D914F3721"

 const val HydroThermalAlertServiceUUIDString = "C6995AF0-4371-4C54-98F9-C01D914F3721"
 const val AlertCountUUIDString = "C699FD19-4371-4C54-98F9-C01D914F3721"
 const val CurrentAlertUUIDString = "C69919EB-4371-4C54-98F9-C01D914F3721"
 const val AlertDateUUIDString = "C699C276-4371-4C54-98F9-C01D914F3721"
 const val AlertTypeUUIDString = "C69910EC-4371-4C54-98F9-C01D914F3721"
 const val AlertInfoUUIDString = "C699ECAD-4371-4C54-98F9-C01D914F3721"

 val HLAServiceUUID = ParcelUuid.fromString(HydroThermalLinearActuatorServieUUIDString)
 val HLACylinderPercentageUUID = ParcelUuid.fromString(HLACylinderPercentageUUIDString)
 val HLACommandUUID = ParcelUuid.fromString(HLACommandUUIDString)
 val HLAStatusUUID = ParcelUuid.fromString(HLAStatusUUIDString)
 val HLADataTransferUUID = ParcelUuid.fromString(HLADataTransferUUIDString)
 val HLADeviceNameUUID = ParcelUuid.fromString(HLADeviceNameUUIDString)
 val HLACylinderPositionUUID = ParcelUuid.fromString(HLACylinderPositionUUIDString)
 val HLAAccelerometerDataUUID = ParcelUuid.fromString(HLAAccelerometerDataUUIDString)
 val HLACylinderTimeUUID = ParcelUuid.fromString(HLACylinderTimeUUIDString)

 val HLAAlertServiceUUID = ParcelUuid.fromString(HydroThermalAlertServiceUUIDString)
 val AlertCountUUID = ParcelUuid.fromString(AlertCountUUIDString)
 val CurrentAlertUUID = ParcelUuid.fromString(CurrentAlertUUIDString)
 val AlertDateUUID = ParcelUuid.fromString(AlertDateUUIDString)
 val AlertTypeUUID = ParcelUuid.fromString(AlertTypeUUIDString)
 val AlertInfoUUID = ParcelUuid.fromString(AlertInfoUUIDString)

 val DeviceInfoServiceUUID = ParcelUuid.fromString(DeviceInformationUUIDString)
 val ManufactureNameUUID = ParcelUuid.fromString(ManufactureNameUUIDString)
 val ModelNumberUUID = ParcelUuid.fromString(ModelNumberUUIDString)
 val SerialNumberUUID = ParcelUuid.fromString(SerialNumberUUIDString)
 val HardwareRevisionUUID = ParcelUuid.fromString(HardwareRevisionUUIDString)
 val FirmwareRevisionUUID = ParcelUuid.fromString(FirmwareRevisionUUIDString)
 val SoftwareRevisionUUID = ParcelUuid.fromString(SoftwareRevisionUUIDString)

}