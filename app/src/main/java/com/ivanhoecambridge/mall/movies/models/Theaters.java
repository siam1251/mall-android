package com.ivanhoecambridge.mall.movies.models;


import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by Kay on 2016-10-26.
 */
//@Root(name = "theaters", strict = false)
@Root(strict = false)
public class Theaters
{
    @Element
    private House house;

    public House getHouse ()
    {
        return house;
    }

    public void setHouse (House house)
    {
        this.house = house;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [house = "+house+"]";
    }
}
