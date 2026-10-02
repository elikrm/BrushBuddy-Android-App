package com.elnaz.brushbuddy.ui.components
import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import com.elnaz.brushbuddy.R
import com.elnaz.brushbuddy.utils.UserAuth

@Preview
@Composable
fun Signup(){
    var email by remember { mutableStateOf("") }
    var password by remember {mutableStateOf("")}

    Column(
        modifier = Modifier
            .fillMaxSize().background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 50.dp)
    )

    {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border( width= 1.dp,
                    color= Color.White,
                    shape = RoundedCornerShape(12.dp
                    )

                )

            ,contentAlignment = Alignment.Center

        ){

            Image(
                painter = painterResource(id=R.drawable.smiley_tooth),
                contentDescription = "main icon smiley",
                contentScale = ContentScale.Crop



            )
        }
        Spacer(modifier = Modifier.height(100.dp))
        Text(
            "Sign up here",
            color = Color.Black,
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 30.dp).align(Alignment.CenterHorizontally)

        )
        TextField(
            value = email,
            onValueChange = { email = it },
            label = {Text("Email")},
            modifier = Modifier.fillMaxWidth().clip(shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp)
                .border(
                    width = 1.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(12.dp)
                )




        )
        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = {Text("password")},
            modifier = Modifier.fillMaxWidth().clip(shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp)
                .border(
                    width = 1.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(12.dp)
                )



        )
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp, horizontal = 40.dp).size(50.dp)
                .border(
                    width = 1.dp,
                    shape = RoundedCornerShape(size = 12.dp)
                    , color = Color.White
                )
                .background(Color.Blue),
            contentAlignment = Alignment.Center,



            ){
            Text (

                "sign up" ,
                color = Color.White
            )
        }

        val annotatedString = buildAnnotatedString {

            pushStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.Blue))
            append("Back to login page")

        }
        val annotatedStringpass = buildAnnotatedString {

            pushStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.Blue))
            append("Forgot password? ")

        }
        Text(
            text = annotatedString,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = annotatedStringpass,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}