package de.fhbingen.mensa.data.orm;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

/**
 * Created by tknapp on 07.11.15.
 */

@Table(name = "Buildings")
public class Building extends Model {

    public Building(){
        super();
    }

    public Building(int buildingId, int seq, String name, String address, Time timeOpenFrom, Time timeOpenTill, int subscribed){
        this.buildingId = buildingId;
        this.seq = seq;
        this.address = address;
        this.timeOpenFrom = timeOpenFrom;
        this.timeOpenTill = timeOpenTill;
        this.subscribed = subscribed;
    }

    public Building(final Building other){
        super();
        this.buildingId   = other.buildingId;
        this.seq          = other.seq;
        this.name         = other.name;
        this.address      = other.address;
        this.timeOpenFrom = other.timeOpenFrom;
        this.timeOpenTill = other.timeOpenTill;
        this.subscribed   = 0;
    }

    @Column(name = "buildingId", unique = true)
    public int buildingId;

    @Column(name = "seq", unique = true)
    public int seq;

    @Column(name = "name")
    public String name;

    @Column(name = "address")
    public String address;

    @Column(name = "timeOpenFrom")
    public Time timeOpenFrom;

    @Column(name = "timeOpenTill")
    public Time timeOpenTill;

    @Column(name = "subscribed")
    @JsonIgnore
    public int subscribed;

    public List<Dish> getDishes(){
        return getMany(Dish.class, "fk_buildingId");
    }

    public int getBuildingId() {
        return buildingId;
    }

    @Override
    public String toString() {
        return String.format(
                "Building [id: %d, seq: %d, name: %s, address: %s, from: %s, till: %s]",
                buildingId,
                seq,
                name,
                address,
                timeOpenFrom,
                timeOpenTill

        );
    }

    public Building update(final Building o) {
        if(o.buildingId == buildingId) {
            if(o.seq > seq){
                name = o.name;
                address = o.address;
                timeOpenTill = o.timeOpenTill;
                timeOpenFrom = o.timeOpenFrom;
                seq = o.seq;
            }
        }
        return this;
    }

    public static Building findByBuildingId(final long buildingId){
        return findByBuildingId(Long.toString(buildingId));
    }

    public static Building findByBuildingId(final String buildingId){
        return new Select().from(Building.class).where("buildingId = ?", buildingId).executeSingle();
    }

    public static List<Building> findAll(){
        return new Select().from(Building.class).orderBy("buildingId").execute();
    }

    public String getName() {
        return name;
    }
}