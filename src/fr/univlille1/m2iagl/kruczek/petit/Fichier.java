package fr.univlille1.m2iagl.kruczek.petit;

import java.util.ArrayList;

public class Fichier {
	public String path;
	public ArrayList lignes =new ArrayList();
	public ArrayList iLignes =new ArrayList();
	
	public Fichier(String path) {
		//System.out.println("creation fichier");
		this.path=path;
	}

}
