package com.moms.app;

import java.util.List;

import pt.tecnico.moms.grpc.Communication;

public interface SearchCompatible {

    /**
     *  Called when information about items is received after a search request is sent
     * @param itemsList
     *  A list containing information about all items that comply with the search parameters
     */
    void searchedItems(List<Communication.ItemInformation> itemsList);
}
