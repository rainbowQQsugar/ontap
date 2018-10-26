package com.abinbev.dsa.adapter.FlexibleData;


public class SortableHeaderDataHandler extends Object{
    private boolean sortedAscending = false;
    private boolean sortedDescending = false;

    public void setSortedAscending(){
        this.sortedAscending = true;
        this.sortedDescending = false;
    }

    public void setSortedDescending(){
        this.sortedAscending = false;
        this.sortedDescending = true;
    }

    public boolean isSorted(){
        return this.sortedAscending || this.sortedDescending;
    }

    public boolean isSortedAscending(){
        return this.sortedAscending;
    }

    public boolean isSortedDescending(){
        return this.sortedDescending;
    }

    public void resetSorting(){
        this.sortedAscending = false;
        this.sortedDescending = false;
    }
}