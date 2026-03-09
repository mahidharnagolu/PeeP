import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Theme } from '@/constants/Colors';
import { useAuthStore } from '@/stores/authStore';
import { useRouter } from 'expo-router';

export default function ProfileScreen() {
    const { profile, signOut } = useAuthStore();
    const router = useRouter();

    const handleSignOut = async () => {
        await signOut();
        router.replace('/auth/login');
    };

    return (
        <View style={styles.container}>
            <View style={styles.header}>
                <Text style={styles.title}>Profile</Text>
            </View>

            <View style={styles.content}>
                <View style={styles.avatarSection}>
                    <View style={styles.avatar}>
                        <Text style={styles.avatarText}>
                            {profile?.username?.charAt(0).toUpperCase() || 'P'}
                        </Text>
                    </View>
                    <Text style={styles.username}>@{profile?.username || 'user'}</Text>
                </View>

                <TouchableOpacity style={styles.signOutBtn} onPress={handleSignOut}>
                    <Text style={styles.signOutText}>Sign Out</Text>
                </TouchableOpacity>
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: Theme.colors.background,
    },
    header: {
        paddingTop: 60,
        paddingHorizontal: 20,
        paddingBottom: 20,
        borderBottomWidth: 1,
        borderBottomColor: Theme.colors.border,
    },
    title: {
        fontSize: 32,
        fontWeight: 'bold',
        color: Theme.colors.text,
    },
    content: {
        flex: 1,
        padding: 20,
        alignItems: 'center',
    },
    avatarSection: {
        alignItems: 'center',
        marginVertical: 40,
    },
    avatar: {
        width: 100,
        height: 100,
        borderRadius: 50,
        backgroundColor: Theme.colors.text,
        justifyContent: 'center',
        alignItems: 'center',
        marginBottom: 16,
    },
    avatarText: {
        fontSize: 40,
        fontWeight: 'bold',
        color: Theme.colors.background,
    },
    username: {
        fontSize: 24,
        fontWeight: 'bold',
        color: Theme.colors.text,
    },
    signOutBtn: {
        backgroundColor: Theme.colors.error,
        paddingHorizontal: 32,
        paddingVertical: 16,
        borderRadius: 12,
        width: '100%',
        alignItems: 'center',
        marginTop: 40,
    },
    signOutText: {
        color: '#FFFFFF',
        fontSize: 18,
        fontWeight: 'bold',
    },
});
