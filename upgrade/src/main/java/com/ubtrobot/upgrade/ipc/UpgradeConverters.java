package com.ubtrobot.upgrade.ipc;

import com.ubtrobot.upgrade.DetectOption;
import com.ubtrobot.upgrade.Firmware;
import com.ubtrobot.upgrade.FirmwarePackage;
import com.ubtrobot.upgrade.FirmwarePackageGroup;

import java.util.LinkedList;
import java.util.List;

public class UpgradeConverters {

    private UpgradeConverters() {
    }

    public static List<Firmware> toFirmwareListPojo(UpgradeProto.FirmwareList firmwareListProto) {
        LinkedList<Firmware> firmwareList = new LinkedList<>();
        for (UpgradeProto.Firmware firmwareProto : firmwareListProto.getFirmwareList()) {
            firmwareList.add(toFirmwarePojo(firmwareProto));
        }

        return firmwareList;
    }

    public static Firmware toFirmwarePojo(UpgradeProto.Firmware firmwareProto) {
        return new Firmware.Builder(firmwareProto.getName(), firmwareProto.getVersion()).
                setUpgradeTime(firmwareProto.getUpgradeTime()).
                setCurrentPackage(toFirmwarePackagePojo(firmwareProto.getCurrentPackage())).
                build();
    }

    public static UpgradeProto.Firmware toFirmwareProto(Firmware firmware) {
        return UpgradeProto.Firmware.newBuilder().
                setName(firmware.getName()).
                setVersion(firmware.getVersion()).
                setUpgradeTime(firmware.getUpgradeTime()).
                setCurrentPackage(totoFirmwarePackageProto(firmware.getCurrentPackage())).
                build();
    }

    public static FirmwarePackage
    toFirmwarePackagePojo(UpgradeProto.FirmwarePackage firmwarePackageProto) {
        return new FirmwarePackage.Builder(
                firmwarePackageProto.getName(),
                firmwarePackageProto.getVersion()).
                setGroup(firmwarePackageProto.getGroup()).
                setForced(firmwarePackageProto.getForced()).
                setIncremental(firmwarePackageProto.getIncremental()).
                setPackageUrl(firmwarePackageProto.getPackageUrl()).
                setPackageMd5(firmwarePackageProto.getPackageMd5()).
                setIncrementUrl(firmwarePackageProto.getIncrementUrl()).
                setIncrementMd5(firmwarePackageProto.getIncrementMd5()).
                setReleaseTime(firmwarePackageProto.getReleaseTime()).
                setReleaseNote(firmwarePackageProto.getReleaseNote()).
                setLocalFile(firmwarePackageProto.getLocalFile()).
                build();
    }

    public static UpgradeProto.FirmwarePackage
    toFirmwarePackageProto(FirmwarePackage firmwarePackage) {
        return UpgradeProto.FirmwarePackage.newBuilder().
                setName(firmwarePackage.getName()).
                setVersion(firmwarePackage.getVersion()).
                setGroup(firmwarePackage.getGroup()).
                setForced(firmwarePackage.isForced()).
                setIncremental(firmwarePackage.isIncremental()).
                setPackageUrl(firmwarePackage.getPackageUrl()).
                setPackageMd5(firmwarePackage.getPackageMd5()).
                setIncrementUrl(firmwarePackage.getIncrementUrl()).
                setIncrementMd5(firmwarePackage.getIncrementMd5()).
                setReleaseTime(firmwarePackage.getReleaseTime()).
                setReleaseNote(firmwarePackage.getReleaseNote()).
                setLocalFile(firmwarePackage.getLocalFile()).
                build();
    }

    public static UpgradeProto.FirmwarePackage
    totoFirmwarePackageProto(FirmwarePackage firmwarePackage) {
        return UpgradeProto.FirmwarePackage.newBuilder().
                setName(firmwarePackage.getName()).
                setVersion(firmwarePackage.getVersion()).
                setGroup(firmwarePackage.getGroup()).
                setForced(firmwarePackage.isForced()).
                setIncremental(firmwarePackage.isIncremental()).
                setPackageUrl(firmwarePackage.getPackageUrl()).
                setPackageMd5(firmwarePackage.getPackageMd5()).
                setIncrementUrl(firmwarePackage.getIncrementUrl()).
                setIncrementMd5(firmwarePackage.getIncrementMd5()).
                setReleaseTime(firmwarePackage.getReleaseTime()).
                setReleaseNote(firmwarePackage.getReleaseNote()).
                setLocalFile(firmwarePackage.getLocalFile()).
                build();
    }

    public static UpgradeProto.FirmwareList toFirmwareListProto(List<Firmware> firmwareList) {
        UpgradeProto.FirmwareList.Builder builder = UpgradeProto.FirmwareList.newBuilder();
        for (Firmware firmware : firmwareList) {
            builder.addFirmware(toFirmwareProto(firmware));
        }

        return builder.build();
    }

    public static UpgradeProto.DetectOption toDetectOptionProto(DetectOption option) {
        return UpgradeProto.DetectOption.newBuilder().
                setRemoteOrLocalSource(option.isRemoteOrLocalSource()).
                setLocalSourcePath(option.getLocalSourcePath()).
                setTimeout(option.getTimeout()).
                build();
    }

    public static DetectOption toDetectOptionPojo(UpgradeProto.DetectOption optionProto) {
        return new DetectOption.Builder(optionProto.getRemoteOrLocalSource()).
                setLocalSourcePath(optionProto.getLocalSourcePath()).
                setTimeout(optionProto.getTimeout()).
                build();
    }

    public static FirmwarePackageGroup
    toFirmwarePackageGroupPojo(UpgradeProto.FirmwarePackageGroup packageGroupProto) {
        FirmwarePackageGroup.Builder builder = new FirmwarePackageGroup.Builder(
                packageGroupProto.getName()).
                setForced(packageGroupProto.getForced()).
                setReleaseTime(packageGroupProto.getReleaseTime()).
                setReleaseNote(packageGroupProto.getReleaseNote());

        for (UpgradeProto.FirmwarePackage firmwarePackageProto :
                packageGroupProto.getFirmwarePackageList()) {
            builder.addPackage(toFirmwarePackagePojo(firmwarePackageProto));
        }

        return builder.build();
    }

    public static UpgradeProto.FirmwarePackageGroup
    toFirmwarePackageGroupProto(FirmwarePackageGroup packageGroup) {
        UpgradeProto.FirmwarePackageGroup.Builder builder = UpgradeProto.
                FirmwarePackageGroup.newBuilder().
                setName(packageGroup.getName()).
                setForced(packageGroup.isForced()).
                setReleaseTime(packageGroup.getReleaseTime()).
                setReleaseNote(packageGroup.getReleaseNote());

        for (FirmwarePackage firmwarePackage : packageGroup) {
            builder.addFirmwarePackage(toFirmwarePackageProto(firmwarePackage));
        }

        return builder.build();
    }
}