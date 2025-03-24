package it.dmi.structure.soglie;

public record SogliaVettore(String id) implements Comparable {

    /**
     * This method in this class returns always true when invoked because the class purpose is
     * to be a dummy threshold to fire all related Azioni through Quartz
     * @param toCompare dummy value
     * @return always true
     * @param <Content> dummy value generic type
     */
    @Override
    public <Content> boolean compare(Content toCompare) {
        return true;
    }
}
