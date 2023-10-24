import java.io.IOException;
import java.io.OutputStream;

public class BitOutputStream {
    private OutputStream outputStream;
    private int currentByte;
    private int numBitsFilled;

    public BitOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.currentByte = 0;
        this.numBitsFilled = 0;
    }

    public void writeBit(boolean bit) throws IOException {
        if (numBitsFilled == 8) {
            outputStream.write(currentByte);
            currentByte = 0;
            numBitsFilled = 0;
        }

        if (bit) {
            currentByte |= (1 << (7 - numBitsFilled));
        }

        numBitsFilled++;
    }

    public void close() throws IOException {
        while (numBitsFilled < 8) {
            writeBit(false); // Pad with zeros if needed
        }

        outputStream.write(currentByte);
        outputStream.close();
    }
}
