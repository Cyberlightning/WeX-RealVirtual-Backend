package com.cyberlightning.webserver.services;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.entities.EntityTable;

public class SaveFileRoutine implements Runnable {
	
	
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(StaticResources.SAVE_TO_HD_INTERVAL);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//if (DataStorageService.getInstance().entityTable.entities.isEmpty()) continue;
			//if (!suspendFlag) continue;
			//DataStorageService.getInstance().saveInProcessFlag = true;
			EntityTable oldEntities = DataStorageService.getInstance().loadData();
			System.out.println("Old Entities:" + oldEntities.entities.size());
			DataStorageService.getInstance().entityTable.appendOldEntities(oldEntities.entities);
			System.out.println("New Entities:" + DataStorageService.getInstance().entityTable.entities.size());
			DataStorageService.getInstance().saveData(DataStorageService.getInstance().entityTable,StaticResources.DATABASE_FILE_NAME);
			DataStorageService.getInstance().entityTable.clearAll();
			DataStorageService.getInstance().saveData(DataStorageService.getInstance().baseStationReferences, StaticResources.REFERENCE_TABLE_FILE_NAME);
			//DataStorageService.getInstance().saveInProcessFlag = false;
			//if (!eventBuffer.isEmpty()) wakeThread();
		}
		
	}

}
