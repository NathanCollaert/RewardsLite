package com.backtobedrock.rewardslite.interfaces;

import com.backtobedrock.rewardslite.domain.CustomHolder;
import com.backtobedrock.rewardslite.domain.Icon;
import com.backtobedrock.rewardslite.interfaces.clickActions.ClickActionNextPage;
import com.backtobedrock.rewardslite.interfaces.clickActions.ClickActionPreviousPage;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPaginatedInterface extends AbstractInterface {
    protected int currentPage = 1;
    protected int totalPages;

    public AbstractPaginatedInterface(CustomHolder customHolder, int dataCount) {
        super(customHolder);
        this.totalPages = (int) Math.ceil((double) dataCount / 28);
    }

    @Override
    protected void initialize() {
        this.customHolder.reset();
        super.initialize();
    }

    @Override
    protected void setData() {
        //Previous page button
        if (this.totalPages > 1 && this.currentPage > 1) {
            this.customHolder.setIcon((this.customHolder.getRowAmount() - 1) * 9 + 3, new Icon(this.previousPageDisplayItem(), Collections.singletonList(new ClickActionPreviousPage(this))));
        }

        //Current page
        if (this.totalPages > 1) {
            this.customHolder.setIcon((this.customHolder.getRowAmount() - 1) * 9 + 4, new Icon(this.pageInformationDisplayItem(), Collections.emptyList()));
        }

        //Next page button
        if (this.totalPages > 1 && this.currentPage < this.totalPages) {
            this.customHolder.setIcon((this.customHolder.getRowAmount() - 1) * 9 + 5, new Icon(this.nextPageDisplayItem(), Collections.singletonList(new ClickActionNextPage(this))));
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

    private ItemStack nextPageDisplayItem() {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("current_page", Integer.toString(currentPage));
            put("total_pages", Integer.toString(totalPages));
            put("next_page", Integer.toString(currentPage + 1));
        }};
        return this.plugin.getConfigurations().getInterfacesConfiguration().getNextPageDisplay().getItem(placeholders);
    }

    private ItemStack previousPageDisplayItem() {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("current_page", Integer.toString(currentPage));
            put("total_pages", Integer.toString(totalPages));
            put("previous_page", Integer.toString(currentPage - 1));
        }};
        return this.plugin.getConfigurations().getInterfacesConfiguration().getPreviousPageDisplay().getItem(placeholders);
    }

    private ItemStack pageInformationDisplayItem() {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("current_page", Integer.toString(currentPage));
            put("total_pages", Integer.toString(totalPages));
        }};
        return this.plugin.getConfigurations().getInterfacesConfiguration().getPageInformationDisplay().getItem(placeholders);
    }
}
