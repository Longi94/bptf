package com.tlongdev.bktf.quickreturn;

public interface QuickReturnStateTransition {
    public int determineState(final int rawY, int quickReturnHeight);
}