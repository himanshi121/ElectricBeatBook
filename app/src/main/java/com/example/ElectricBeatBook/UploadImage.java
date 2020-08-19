package com.example.ElectricBeatBook;

public class UploadImage {
    private String Name;
    private String ImageUrl;

    private String Details;

    public UploadImage(String imageUrl,String name, String details) {
        ImageUrl = imageUrl;
        Name = name;
        Details= details;
    }


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


}
