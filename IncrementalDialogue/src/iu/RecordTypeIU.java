package iu;

import inpro.incremental.unit.IU;
import qmul.ds.formula.TTRRecordType;

public class RecordTypeIU extends IU {
	
	TTRRecordType recordType;
	
	public TTRRecordType getRecordType() {
		return recordType;
	}

	public void setRecordType(TTRRecordType recordType) {
		this.recordType = recordType;
	}

	@Override
	public String toPayLoad() {
		return this.getRecordType().toString();
	}

}