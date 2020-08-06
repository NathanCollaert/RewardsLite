package com.backtobedrock.LitePlaytimeRewards.guis;

import com.backtobedrock.LitePlaytimeRewards.guis.clickActions.ClickAction;
import com.backtobedrock.LitePlaytimeRewards.guis.clickActions.NextPageClickAction;
import com.backtobedrock.LitePlaytimeRewards.guis.clickActions.PreviousPageClickAction;
import java.util.Arrays;
import org.bukkit.Material;

public abstract class PagedGUI extends GUI {

    protected int currentPage = 1;
    protected int totalPages = 1;

    public PagedGUI(CustomHolder customHolder, int totalPages) {
        super(customHolder);
        this.totalPages = totalPages;
    }

    @Override
    public void initialize() {
        this.customHolder.clearContent();
        super.initialize();
        this.setPageControls();
        this.setData();
    }

    private void setPageControls() {
        //Previous page button
        if (this.totalPages > 1 && this.currentPage > 1) {
            this.customHolder.setIcon((this.customHolder.getRowAmount() - 1) * 9 + 3, new Icon(this.createGUIItem("« Previous Page", Arrays.asList(), false, Material.STONE_BUTTON), Arrays.asList(new ClickAction[]{new PreviousPageClickAction(this)}), null));
        }

        //Current page
        this.customHolder.setIcon((this.customHolder.getRowAmount() - 1) * 9 + 4, new Icon(this.createGUIItem(String.format("Page %s/%s", this.currentPage, this.totalPages), Arrays.asList(), true, Material.PAPER), Arrays.asList(), null));

        //Next page button
        if (this.totalPages > 1 && this.currentPage < this.totalPages) {
            this.customHolder.setIcon((this.customHolder.getRowAmount() - 1) * 9 + 5, new Icon(this.createGUIItem("Next Page »", Arrays.asList(), false, Material.STONE_BUTTON), Arrays.asList(new ClickAction[]{new NextPageClickAction(this)}), null));
        }
    }

    protected abstract void setData();

    public void nextPage() {
        if (this.currentPage < this.totalPages) {
            this.currentPage++;
        }
    }

    public void previousPage() {
        if (this.currentPage > 1) {
            this.currentPage--;
        }
    }
}
