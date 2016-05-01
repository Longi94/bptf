/**
 * Copyright 2016 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Long
 * @since 2016. 03. 19.
 */
public class User implements Parcelable {

    private String name;

    private String steamId;

    private String resolvedSteamId;

    private String avatarUrl;

    private int reputation;

    private long profileCreated;

    private int state;

    private long lastOnline;

    private boolean inGroup;

    private boolean banned;

    private boolean scammer;

    private boolean economyBanned;

    private boolean vacBanned;

    private boolean communityBanned;

    private double backpackValue;

    private long lastUpdated;

    private int trustPositive;

    private int trustNegative;

    private int rawKeys;

    private double rawMetal;

    private int backpackSlots;

    private int itemCount;

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public User() {}

    public User(Parcel source) {
        name = source.readString();
        steamId = source.readString();
        resolvedSteamId = source.readString();
        avatarUrl = source.readString();
        reputation = source.readInt();
        profileCreated = source.readLong();
        state = source.readInt();
        lastOnline = source.readLong();
        inGroup = source.readByte() == 1;
        banned = source.readByte() == 1;
        scammer = source.readByte() == 1;
        economyBanned = source.readByte() == 1;
        vacBanned = source.readByte() == 1;
        communityBanned = source.readByte() == 1;
        backpackValue = source.readDouble();
        lastUpdated = source.readLong();
        trustPositive = source.readInt();
        trustNegative = source.readInt();
        rawKeys = source.readInt();
        rawMetal = source.readDouble();
        backpackSlots = source.readInt();
        itemCount = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(steamId);
        dest.writeString(resolvedSteamId);
        dest.writeString(avatarUrl);
        dest.writeInt(reputation);
        dest.writeLong(profileCreated);
        dest.writeInt(state);
        dest.writeLong(lastOnline);
        dest.writeByte((byte) (inGroup ? 1 : 0));
        dest.writeByte((byte) (banned ? 1 : 0));
        dest.writeByte((byte) (scammer ? 1 : 0));
        dest.writeByte((byte) (economyBanned ? 1 : 0));
        dest.writeByte((byte) (vacBanned ? 1 : 0));
        dest.writeByte((byte) (communityBanned ? 1 : 0));
        dest.writeDouble(backpackValue);
        dest.writeLong(lastUpdated);
        dest.writeInt(trustPositive);
        dest.writeInt(trustNegative);
        dest.writeInt(rawKeys);
        dest.writeDouble(rawMetal);
        dest.writeInt(backpackSlots);
        dest.writeInt(itemCount);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSteamId() {
        return steamId;
    }

    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }

    public String getResolvedSteamId() {
        return resolvedSteamId;
    }

    public void setResolvedSteamId(String resolvedSteamId) {
        this.resolvedSteamId = resolvedSteamId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public long getProfileCreated() {
        return profileCreated;
    }

    public void setProfileCreated(long profileCreated) {
        this.profileCreated = profileCreated;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public boolean isEconomyBanned() {
        return economyBanned;
    }

    public void setEconomyBanned(boolean economyBanned) {
        this.economyBanned = economyBanned;
    }

    public boolean isVacBanned() {
        return vacBanned;
    }

    public void setVacBanned(boolean vacBanned) {
        this.vacBanned = vacBanned;
    }

    public boolean isCommunityBanned() {
        return communityBanned;
    }

    public void setCommunityBanned(boolean communityBanned) {
        this.communityBanned = communityBanned;
    }

    public double getBackpackValue() {
        return backpackValue;
    }

    public void setBackpackValue(double backpackValue) {
        this.backpackValue = backpackValue;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public int getTrustPositive() {
        return trustPositive;
    }

    public void setTrustPositive(int trustPositive) {
        this.trustPositive = trustPositive;
    }

    public int getTrustNegative() {
        return trustNegative;
    }

    public void setTrustNegative(int trustNegative) {
        this.trustNegative = trustNegative;
    }

    public int getRawKeys() {
        return rawKeys;
    }

    public void setRawKeys(int rawKeys) {
        this.rawKeys = rawKeys;
    }

    public double getRawMetal() {
        return rawMetal;
    }

    public void setRawMetal(double rawMetal) {
        this.rawMetal = rawMetal;
    }

    public int getBackpackSlots() {
        return backpackSlots;
    }

    public void setBackpackSlots(int backpackSlots) {
        this.backpackSlots = backpackSlots;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public boolean isInGroup() {
        return inGroup;
    }

    public void setInGroup(boolean inGroup) {
        this.inGroup = inGroup;
    }

    public boolean isScammer() {
        return scammer;
    }

    public void setScammer(boolean scammer) {
        this.scammer = scammer;
    }
}
