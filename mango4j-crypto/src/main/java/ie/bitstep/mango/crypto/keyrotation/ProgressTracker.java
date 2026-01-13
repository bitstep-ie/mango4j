package ie.bitstep.mango.crypto.keyrotation;

import ie.bitstep.mango.crypto.keyrotation.exceptions.TooManyFailuresException;

public class ProgressTracker {

	private final int maxFailureCountPerExecution;
	private int numberOfRecordsProcessed = 0;
	private int numberOfRecordsFailed = 0;
	private int numberOfBatchesProcessed = 0;

	public ProgressTracker(int maxFailureCountPerExecution) {
		this.maxFailureCountPerExecution = maxFailureCountPerExecution;
	}

	public void incrementRecordsProcessed() {
		numberOfRecordsProcessed += 1;
	}

	public void incrementBatchesProcessed() {
		numberOfBatchesProcessed += 1;
	}

	public void incrementNumberOfRecordsFailed() {
		if (++numberOfRecordsFailed > maxFailureCountPerExecution && maxFailureCountPerExecution >= 0) {
			throw new TooManyFailuresException(
				String.format("Max errors threshold of %d per execution exceeded while processing records, failure count=%d",
					maxFailureCountPerExecution, numberOfRecordsFailed));
		}
	}

	public int getNumberOfBatchesProcessed() {
		return numberOfBatchesProcessed;
	}

	public int getNumberOfRecordsFailed() {
		return numberOfRecordsFailed;
	}

	public int getNumberOfRecordsProcessed() {
		return numberOfRecordsProcessed;
	}
}