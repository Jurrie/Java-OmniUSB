package javax.usb3.ri;

public interface ProcessIrpCallback
{
	void onTransferComplete(int actualLength);

	void onTransferAborted();

	void onTransferError(String strError);

	void onTransferCancelled(String strError);

	void onTransferStall(String strError);

	void onTransferNoDevice(String strError);

	void onTransferOverflow(String strError);

	void onTransferTimedOut(String strError);

	void onControlRequestNotSupported(String strError);

	public static abstract class WrappingProcessIrpCallback implements ProcessIrpCallback
	{
		private final ProcessIrpCallback wrappedCallback;

		public WrappingProcessIrpCallback(final ProcessIrpCallback wrappedCallback)
		{
			this.wrappedCallback = wrappedCallback;
		}

		@Override
		public void onTransferComplete(final int actualLength)
		{
			wrappedCallback.onTransferComplete(actualLength);
		}

		@Override
		public void onTransferAborted()
		{
			wrappedCallback.onTransferAborted();
		}

		@Override
		public void onTransferError(final String strError)
		{
			wrappedCallback.onTransferError(strError);
		}

		@Override
		public void onTransferCancelled(final String strError)
		{
			wrappedCallback.onTransferCancelled(strError);
		}

		@Override
		public void onTransferStall(final String strError)
		{
			wrappedCallback.onTransferStall(strError);
		}

		@Override
		public void onTransferNoDevice(final String strError)
		{
			wrappedCallback.onTransferNoDevice(strError);
		}

		@Override
		public void onTransferOverflow(final String strError)
		{
			wrappedCallback.onTransferOverflow(strError);
		}

		@Override
		public void onTransferTimedOut(final String strError)
		{
			wrappedCallback.onTransferTimedOut(strError);
		}

		@Override
		public void onControlRequestNotSupported(final String strError)
		{
			wrappedCallback.onControlRequestNotSupported(strError);
		}
	}
}
