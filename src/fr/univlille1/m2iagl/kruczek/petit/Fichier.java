package fr.univlille1.m2iagl.kruczek.petit;

import java.util.ArrayList;

public class Fichier {
	public String path;
	public ArrayList<String> lignes =new ArrayList<String>();
	public ArrayList<String> iLignes =new ArrayList<String>();
	
	public Fichier(String path) {
		this.path=path;
	}

}
