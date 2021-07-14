package siege.exeptions;

/**
 * exception class to notify siege errors
 * 
 * @author Tommaso
 *
 */
public final class SiegeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SiegeException(String msg) {
		super(msg);
	}

}
