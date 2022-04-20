package com.backtobedrock.LitePlaytimeRewards.guis;

import com.backtobedrock.LitePlaytimeRewards.guis.clickActions.ClickAction;
import com.backtobedrock.LitePlaytimeRewards.guis.clickActions.NextPageClickAction;
import com.backtobedrock.LitePlaytimeRewards.guis.clickActions.PreviousPageClickAction;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collections;

public abstract class PagedGUI extends GUI {

    protected int currentPage = 1;
    protected int totalPages = 1;

    public PagedGUI(CustomHolder customHolder, int dataSize) {
        super(customHolder);
        this.totalPages = (int) Math.ceil((double) dataSize / 28);
    }

    @Override
    public void initialize() {
        this.customHolder.reset();
        super.initialize();
    }

    @Override
    protected void setData() {
        //Previous page button
        if (this.totalPages > 1 && this.currentPage > 1) {
            this.customHolder.setIcon((this.customHolder.getRowAmount() - 1) * 9 + 3, new Icon(this.createGUIItem("« Previous Page", Collections.emptyList(), false, Material.STONE_BUTTON), Arrays.asList(new ClickAction[]{new PreviousPageClickAction(this)}), null));
        }

        //Current page
        if (this.totalPages > 1) {
            this.customHolder.setIcon((this.customHolder.getRowAmount() - 1) * 9 + 4, new Icon(this.createGUIItem(String.format("Page %s/%s", this.currentPage, this.totalPages), Collections.emptyList(), true, Material.PAPER), Collections.emptyList(), null));
        }

        //Next page button
        if (this.totalPages > 1 && this.currentPage < this.totalPages) {
            this.customHolder.setIcon((this.customHolder.getRowAmount() - 1) * 9 + 5, new Icon(this.createGUIItem("Next Page »", Collections.emptyList(), false, Material.STONE_BUTTON), Arrays.asList(new ClickAction[]{new NextPageClickAction(this)}), null));
        }
    }

    public void nextPage() {
        if (this.currentPage < this.totalPages) {
            this.currentPage++;
            this.initialize();
            this.customHolder.updateInvent();
        }
    }

    public void previousPage() {
        if (this.currentPage > 1) {
            this.currentPage--;
            this.initialize();
            this.customHolder.updateInvent();
        }
    }
}
