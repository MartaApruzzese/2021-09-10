package it.polito.tdp.yelp.model;

import java.util.*;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.yelp.db.YelpDao;

public class Model {
	private YelpDao dao;
	private Graph<Business, DefaultWeightedEdge> grafo;
	private List<Business> vertici;
	private Map<String, Business> idMap;
	double max;
	private List<Business> best;
	private double kmTot;
	
	public Model() {
		this.dao= new YelpDao();
	}
	
	
	public void creaGrafo(String citta) {
		this.grafo= new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		this.vertici= new ArrayList<>();
		this.idMap= new HashMap<>();
		
		//Popolo idMap
		for(Business b: this.dao.getAllBusiness()) {
			idMap.put(b.getBusinessId(), b);
		}
		
		
		//Creo i vertici;
		for(String id: this.dao.getBusinessPerCitta(citta, idMap)) {
			Business b= this.idMap.get(id);
			this.vertici.add(b);
		}
		Graphs.addAllVertices(this.grafo, this.vertici);
		
		//Creo gli archi
		/*for(Business b: this.vertici) {
			for(Business b2: this.vertici) {
				if(!b.equals(b2) && !this.grafo.containsEdge(b2, b) && !this.grafo.containsEdge(b, b2) ) {
					double peso= this.dao.getPesoDatiVertici(b.getBusinessId(), b2.getBusinessId());
					Graphs.addEdgeWithVertices(this.grafo, b, b2, peso);
				}
			}
		}*/
		
		//Nel creare i business ho gia lat e long.
		for(Business b1: this.vertici) {
			for(Business b2: this.vertici) {
				if(!b1.equals(b2) && !this.grafo.containsEdge(b2, b1)) {
					LatLng punto1= new LatLng(b1.getLatitude(), b1.getLongitude());
					LatLng punto2= new LatLng(b2.getLatitude(), b2.getLongitude());
					double peso= LatLngTool.distance(punto1, punto2, LengthUnit.KILOMETER);
					Graphs.addEdgeWithVertices(this.grafo, b1, b2, peso);
 				}
			}
		}
		
		
		
	}
	
	/**
	 * PUNTO 1d
	 */
	public Business getBusinessLontano(Business partenza) {
		List<Business> vicini= Graphs.neighborListOf(this.grafo, partenza);
		this.max=0.0;
		Business lontano=null;
	
		for(Business b: vicini) {
			double peso= this.grafo.getEdgeWeight(this.grafo.getEdge(partenza, b));
			if(peso>max) {
				max=peso;
				lontano=b;
			}
		}
		return lontano;
	}
	
	public double getDistanzaMax() {
		return this.max;
	}
	
	public List<String> getAllCitta(){
		return this.dao.getAllCitta();
	}
	
	public List<Business> getVertici(){
		return this.vertici;
	}
	
	public int getNVertici() {
		return this.vertici.size();
	}
	
	public int getNArchi() {
		return this.grafo.edgeSet().size();
	}
	
	
	
	/**
	 * RISOLUZIONE RICORSIONE
	 */
	public List<Business> calcolaPercorso(Business source, Business target, double star){
		this.best= new ArrayList<>();
		this.kmTot=0.0;
		
		List<Business> parziale= new ArrayList<Business>();
		
		parziale.add(source);
		
		cerca(parziale, target, 0.0, star);
		return best;
	}


	private void cerca(List<Business> parziale, Business target, double d, double star) {
		 
		//Condizione di fine
		if(parziale.get(parziale.size()-1).equals(target)) {
			//siamo arrivati al target, controllo se è la best
			if(parziale.size()>best.size()) {
				this.best= new ArrayList<>(parziale);
				this.kmTot= d;
			}
			
		}
		
		//Ricorsione
		for(Business b: Graphs.neighborListOf(this.grafo, parziale.get(parziale.size()-1))) {
			if(!parziale.contains(b)) {
				
				Business precedente= parziale.get(parziale.size()-1);
				parziale.add(b);
				DefaultWeightedEdge e= this.grafo.getEdge(precedente, b);
				double peso= this.grafo.getEdgeWeight(e);
				if(b.getStars()>=star) {
					parziale.add(b);
					cerca(parziale,target, d+peso, star);
					parziale.remove(parziale.size()-1);
				}
			}
		}
		
		
	}
	
	
	
	
	
	
}
