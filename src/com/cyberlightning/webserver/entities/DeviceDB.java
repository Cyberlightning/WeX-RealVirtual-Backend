package com.cyberlightning.webserver.entities;

import java.util.HashMap;

public class DeviceDB implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8536303237731902808L;
	private HashMap<String,Entity> dataBase = new HashMap<String,Entity>();
	
	
	public boolean hasEntity(String _uuid) {
		
		if (this.dataBase.containsKey(_uuid)) {
			return true;	
		} else {
			return false;
		}
	}
	
	public void addEntity(Entity _entity) {
		this.dataBase.putIfAbsent(_entity.uuid, _entity);
	}
	
	public void removeEntity(Entity _entity) {
		this.dataBase.remove(_entity.uuid);
	}
	
	public void updateEntity(Entity _entity) {
		this.dataBase.replace(_entity.uuid, _entity);
	}

}
