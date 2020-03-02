package com.arcsoft.arcfacedemo.model;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;

public class FacePreviewInfo {
    private FaceInfo faceInfo;
    private LivenessInfo livenessInfo;
    private AgeInfo ageInfo;
    private GenderInfo genderInfo;
    private int trackId;

    public FacePreviewInfo(FaceInfo faceInfo, LivenessInfo livenessInfo, AgeInfo ageInfo, GenderInfo genderInfo, int trackId) {
        this.faceInfo = faceInfo;
        this.livenessInfo = livenessInfo;
        this.ageInfo = ageInfo;
        this.genderInfo = genderInfo;
        this.trackId = trackId;
    }

    public FaceInfo getFaceInfo() {
        return faceInfo;
    }

    public void setFaceInfo(FaceInfo faceInfo) {
        this.faceInfo = faceInfo;
    }

    public LivenessInfo getLivenessInfo() {
        return livenessInfo;
    }

    public void setLivenessInfo(LivenessInfo livenessInfo) {
        this.livenessInfo = livenessInfo;
    }

    public AgeInfo getAgeInfo(){
        return ageInfo;
    }

    public void setAgeInfo(AgeInfo ageInfo){
        this.ageInfo = ageInfo;
    }

    public GenderInfo getGenderInfo(){
        return genderInfo;
    }

    public void setGenderInfo(GenderInfo genderInfo){
        this.genderInfo = genderInfo;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }
}
