package com.example.ElectricBeatBook;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UploadImage {
    private String Name;
    private String ImageUrl;
  /* private @ServerTimestamp
    Date CreateDate;*/
    private String Details;
    private String Timestamp;
   // private String timestamp;   //Date createDate

    public UploadImage(String imageUrl, String name, String details ,String timestamp) {
        ImageUrl = imageUrl;
        Name = name;
        Details= details;
       Timestamp = timestamp;
       // CreateDate= createDate;

    }

/*
public String toString(){
        return "Date"+createDate;
}
*/

    //public setters and getters for the fields

    public void setTimestamp( String timeStamp) {
        Timestamp= timeStamp;}
    public String getTimestamp() {
        return Timestamp;}
   /* public Date getCreateDate() {
        return CreateDate ;
    }

    public void setCreateDate(Date createDate) {
        this.CreateDate = createDate;
    }
*/
    public String getDetails() {
        return Details;
    }

    public void setDetails(String details) {

        Details = details;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }


   /* public String toString(){
        return "Date"+ CreateDate;
    }
*/
}
