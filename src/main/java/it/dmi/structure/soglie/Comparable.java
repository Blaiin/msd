package it.dmi.structure.soglie;

public sealed interface Comparable permits SogliaContenuto, SogliaInferiore, SogliaRange, SogliaSuperiore, SogliaVettore {

    String id();

    <Content> boolean compare(Content toCompare);
}
