package com.cyberlightning.webserver.entities;

import java.util.HashMap;

public class EntityTable implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8536303237731902808L;
	private HashMap<String,Entity> entities = new HashMap<String,Entity>();
	
	
	
	public boolean hasEntity(String _uuid) {
		
		if (this.entities.containsKey(_uuid)) {
			return true;	
		} else {
			return false;
		}
	}
	
	public void addEntity(Entity _entity) {
		this.entities.put(_entity.uuid, _entity);
	}
	
	public void removeEntity(Entity _entity) {
		this.entities.remove(_entity.uuid);
	}
	

}
