import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.example.ConfigToTomlConverter.runner;

public class main {



    @Test
    public void testFull() {
        String[] s = new String[2];
        s[0]="C:\\Users\\nazar\\OneDrive\\Desktop\\study\\thirdConfig\\src\\main\\resources\\input.txt";
        s[1]="C:\\Users\\nazar\\OneDrive\\Desktop\\study\\thirdConfig\\src\\main\\resources\\output.toml";
        runner(s);
        Assertions.assertEquals(new File(s[1]).exists(), true );
    }
    @Test
    public void testError() {
        String[] s = new String[2];
        s[0]="C:\\Users\\nazar\\OneDrive\\Desktop\\study\\thirdConfig\\src\\main\\resources\\input1.txt";
        s[1]="C:\\Users\\nazar\\OneDrive\\Desktop\\study\\thirdConfig\\src\\main\\resources\\output1.toml";
        runner(s);
        Assertions.assertEquals(new File(s[1]).exists(), false );
    }
}
