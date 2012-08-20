package name.khoobyar.joe.gsspi.win32;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public interface Sspi
	extends com.sun.jna.platform.win32.Sspi
{
	
    /**
     * Provides the name, in a proprietary format, of the user principal associated with a context or handle.
     */
	public static class SecPkgContext_Names extends Structure {
        public static class ByReference extends SecPkgContext_Names implements Structure.ByReference { }
        
		public Pointer sUserName;
		
		public SecPkgContext_Names() { this.sUserName = null; }
		public SecPkgContext_Names(Pointer sUserName) { this.sUserName = sUserName; }
		
		@Override
		protected void finalize() throws Throwable {
			dispose ();
			super.finalize ();
		}
		
		public void dispose () {
			if (sUserName != null)
				Secur32.INSTANCE.FreeContextBuffer (sUserName);
			writeField ("sUserName", null);
		}
		
		public String getUserName () {
			return sUserName==null ? null : sUserName.getString (0, ! Boolean.getBoolean("w32.ascii"));
		}
	}

	/** TimeStamp doesn't define a nested ByReference class, so... */
    public static class TimeStampByReference extends TimeStamp implements Structure.ByReference { }
	
    /**
     * A pointer to a TimeStamp
     */
    public static class PTimeStamp extends Structure {

        /** The first entry in an array of {@link TimeStamp} structures. */
        public TimeStampByReference pTimeStamp;
		
        public PTimeStamp() { }

        public PTimeStamp(TimeStamp h) {
            super (h.getPointer());
            read ();
        }
		
        /** An array of SecPkgInfo structures. */
        public TimeStampByReference[] toArray (int size) {
            return (TimeStampByReference[]) pTimeStamp.toArray (size);
        }
    }
  
    /**
     * Workaround for bugs in JNA platform's version of this class.
     */
	public static class SecBufferDesc
		extends com.sun.jna.platform.win32.Sspi.SecBufferDesc
	{
	    private transient Object buffers[];
	    
	    /** Create a new SecBufferDesc. */
	    public SecBufferDesc (SecBuffer.ByReference buffers[]) {
	    	this.ulVersion = new NativeLong(SECBUFFER_VERSION);
	    	this.cBuffers = new NativeLong(buffers.length);
	    	this.pBuffers = new SecBuffer.ByReference[] { buffers[0] };
	    	this.buffers = buffers;
	    	allocateMemory ();
	    }
	    
	    /** Create a new SecBufferDesc with one buffer. */
	    public SecBufferDesc (SecBuffer.ByReference buffer) {
	    	this ((SecBuffer.ByReference[]) buffer.toArray (1));
	    }
	    
	    /** Create a new SecBufferDesc with one SECBUFFER_EMPTY buffer. */
	    public SecBufferDesc () {
	    	this (new SecBuffer.ByReference ());
	    }
	    
	    /**
	     * Create a new SecBufferDesc with initial data.
	     * @param type    Token type.
	     * @param token   Initial token data.
	     */
	    public SecBufferDesc (int type, byte[] token) {
	    	this (new SecBuffer.ByReference(type, token));
	    }
	    
	    /**
	     * Create a new SecBufferDesc with one SecBuffer of a given type and size.
	     * @param type
	     * @param tokenSize
	     */
	    public SecBufferDesc (int type, int tokenSize) {
	    	this (new SecBuffer.ByReference (type, tokenSize));
	    }
	    
	    /**
	     * Create a new SecBufferDesc with the given number of buffers.
	     * @param type
	     * @param tokenSize
	     */
	    public SecBufferDesc (int buffers) {
	    	this ((SecBuffer.ByReference[]) new SecBuffer.ByReference().toArray (2));
	    }
	    
	    private void syncArray () {
    		if (buffers!=null && buffers[0] == pBuffers[0])
				return;
			buffers = pBuffers[0].toArray (cBuffers.intValue ());
			pBuffers[0] = (SecBuffer.ByReference) buffers[0];
	    }
	    
	    public SecBuffer.ByReference getBuffer (int buffer) {
	    	if (pBuffers[0] == null || cBuffers == null)
	    		throw new RuntimeException("pBuffers | cBuffers");
	    	if (cBuffers.intValue () < buffer)
		    	throw new RuntimeException("cBuffers < "+buffer);
	    	syncArray ();
    		return (SecBuffer.ByReference) buffers[buffer];
	    }
	    
	    public byte[] getBytes(int buffer) {
	    	SecBuffer.ByReference secBuffer = getBuffer (buffer);
	    	return secBuffer.cbBuffer.intValue()==0 ? null : secBuffer.getBytes ();
	    }
	    
	    public byte[] getBytes() {
			return getBytes (0);
		}
	}
	
	// Buffer types.
	int SECBUFFER_PKG_PARAMS = 3;
	int SECBUFFER_MISSING = 4;
	int SECBUFFER_EXTRA = 5;
	int SECBUFFER_STREAM_TRAILER = 6;
	int SECBUFFER_STREAM_HEADER = 7;
	int SECBUFFER_PADDING = 9;
	int SECBUFFER_STREAM = 10;
	int SECBUFFER_MECHLIST = 11;
	int SECBUFFER_MECHLIST_SIGNATURE = 12;
	int SECBUFFER_TARGET = 13;
	int SECBUFFER_CHANNEL_BINDINGS = 14;
	int SECBUFFER_CHANGE_PASS_RESPONSE = 15;
	int SECBUFFER_TARGET_HOST = 16;
	int SECBUFFER_READONLY = 0x80000000;
	int SECBUFFER_READONLY_WITH_CHECKSUM = 0x10000000;
	int SECBUFFER_ATTRMASK = 0xf0000000;

	// Security package attribute structures.
	int SECPKG_ATTR_SIZES                 = 0x00000000;
	int SECPKG_ATTR_NAMES                 = 0x00000001;
    int SECPKG_ATTR_LIFESPAN              = 0x00000002;
    int SECPKG_ATTR_DCE_INFO              = 0x00000003;
    int SECPKG_ATTR_STREAM_SIZES          = 0x00000004;
    int SECPKG_ATTR_KEY_INFO              = 0x00000005;
    int SECPKG_ATTR_AUTHORITY             = 0x00000006;
    int SECPKG_ATTR_PROTO_INFO            = 0x00000007;
    int SECPKG_ATTR_PASSWORD_EXPIRY       = 0x00000008;
    int SECPKG_ATTR_SESSION_KEY           = 0x00000009;
    int SECPKG_ATTR_PACKAGE_INFO          = 0x0000000A;
    int SECPKG_ATTR_NATIVE_NAMES          = 0x0000000D;

	// Additional flags for calls to InitializeSecurityContext
	int ISC_REQ_USE_SESSION_KEY         = 0x00000020;
	int ISC_REQ_PROMPT_FOR_CREDS        = 0x00000040;
	int ISC_REQ_USE_SUPPLIED_CREDS      = 0x00000080;
	int ISC_REQ_USE_DCE_STYLE           = 0x00000200;
	int ISC_REQ_DATAGRAM                = 0x00000400;
	int ISC_REQ_CALL_LEVEL              = 0x00001000;
	int ISC_REQ_FRAGMENT_SUPPLIED       = 0x00002000;
	int ISC_REQ_IDENTIFY                = 0x00020000;
	int ISC_REQ_NULL_SESSION            = 0x00040000;
	int ISC_REQ_MANUAL_CRED_VALIDATION  = 0x00080000;
	// int ISC_REQ_RESERVED1                = 0x00100000;
	int ISC_REQ_FRAGMENT_TO_FIT         = 0x00200000;
	
	int ISC_RET_DELEGATE                = 0x00000001;
	int ISC_RET_MUTUAL_AUTH             = 0x00000002;
	int ISC_RET_REPLAY_DETECT           = 0x00000004;
	int ISC_RET_SEQUENCE_DETECT         = 0x00000008;
	int ISC_RET_CONFIDENTIALITY         = 0x00000010;
	int ISC_RET_USE_SESSION_KEY         = 0x00000020;
	int ISC_RET_USED_COLLECTED_CREDS    = 0x00000040;
	int ISC_RET_USED_SUPPLIED_CREDS     = 0x00000080;
	int ISC_RET_ALLOCATED_MEMORY        = 0x00000100;
	int ISC_RET_USED_DCE_STYLE          = 0x00000200;
	int ISC_RET_DATAGRAM                = 0x00000400;
	int ISC_RET_CONNECTION              = 0x00000800;
	int ISC_RET_INTERMEDIATE_RETURN     = 0x00001000;
	int ISC_RET_CALL_LEVEL              = 0x00002000;
	int ISC_RET_EXTENDED_ERROR          = 0x00004000;
	int ISC_RET_STREAM                  = 0x00008000;
	int ISC_RET_INTEGRITY               = 0x00010000;
	int ISC_RET_IDENTIFY                = 0x00020000;
	int ISC_RET_NULL_SESSION            = 0x00040000;
	int ISC_RET_MANUAL_CRED_VALIDATION  = 0x00080000;
	// int ISC_RET_RESERVED1                = 0x00100000;
	int ISC_RET_FRAGMENT_ONLY           = 0x00200000;

}
