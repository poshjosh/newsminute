package com.looseboxes.idisc.common.listeners;

/**
 * Created by Josh on 8/8/2016.
 */
public interface VerticalScrollListener {

    void onExceedBottom();
    void onReachedBottom();
    void onScrollStopped();
    void onExceedTop();
    void onReachedTop();
}
