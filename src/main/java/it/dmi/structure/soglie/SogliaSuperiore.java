package it.dmi.structure.soglie;

public record SogliaSuperiore(String id, Double value) implements Comparable {

    @Override
    public <Content> boolean compare(Content toCompare) {
        return switch (toCompare) {
            case Integer i -> compareInt(i);
            case Long l -> compareLong(l);
            case Double d -> compareDouble(d);
            case null, default -> false;
        };
    }

    private boolean compareInt(int toCompare) {
        return toCompare > value.intValue();
    }

    private boolean compareLong(long toCompare) {
        return toCompare > value.longValue();
    }

    private boolean compareDouble(double toCompare) {
        return toCompare > value;
    }
}
