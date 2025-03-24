package it.dmi.structure.soglie;

public record SogliaRange(String id, Double inferiore, Double superiore) implements Comparable {

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
        return toCompare >= inferiore.intValue() && toCompare <= superiore.intValue();
    }

    private boolean compareLong(long toCompare) {
        return toCompare >= inferiore.longValue() && toCompare <= superiore.longValue();
    }

    private boolean compareDouble(double toCompare) {
        return toCompare >= inferiore && toCompare <= superiore;
    }
}
